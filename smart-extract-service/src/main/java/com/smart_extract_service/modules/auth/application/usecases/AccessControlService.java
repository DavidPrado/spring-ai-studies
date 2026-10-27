package com.smart_extract_service.modules.auth.application.usecases;

import com.smart_extract_service.core.config.SystemConstants;
import com.smart_extract_service.core.context.Auditable;
import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.modules.auth.application.dto.*;
import com.smart_extract_service.modules.auth.domain.model.Permission;
import com.smart_extract_service.modules.auth.domain.model.Role;
import com.smart_extract_service.modules.auth.domain.model.Tenant;
import com.smart_extract_service.modules.auth.domain.ports.input.RolePermissionUseCase;
import com.smart_extract_service.modules.auth.domain.ports.output.PermissionRepositoryPort;
import com.smart_extract_service.modules.auth.domain.ports.output.RoleRepositoryPort;
import com.smart_extract_service.modules.auth.domain.ports.output.TenantRepositoryPort;
import com.smart_extract_service.modules.auth.domain.ports.output.UserRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccessControlService implements RolePermissionUseCase {

    private final RoleRepositoryPort roleRepository;
    private final PermissionRepositoryPort permissionRepository;
    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;

    public AccessControlService(RoleRepositoryPort roleRepository,
                                PermissionRepositoryPort permissionRepository,
                                TenantRepositoryPort tenantRepository,
                                UserRepositoryPort userRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    // ── Create ─────────────────────────────────────────────────
    @Override
    @Auditable
    @Transactional
    public void createRole(String name, UUID tenantId, RoleRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException("auth.tenant.not_found"));

        List<Permission> permissions = permissionRepository.findAllById(request.permissionIds());

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setDisplayName(request.displayName() != null && !request.displayName().isBlank()
                ? request.displayName()
                : prettify(name));
        role.setName(name);
        role.setTenantId(tenant.getId());
        role.setPermissions(new HashSet<>(permissions));
        role.setIsSystemDefault(false);

        roleRepository.save(role);
    }

    private String prettify(String name) {
        String base = name.replaceFirst("^ROLE_", "").replace('_', ' ').toLowerCase();
        if (base.isEmpty()) return name;
        return base.substring(0, 1).toUpperCase() + base.substring(1);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<RoleSummaryResponse> listRoles(UUID tenantId, Map<String, String> filters, Pageable pageable) {
        return toSummaryPage(roleRepository.findAll(tenantId, filters, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleSummaryResponse> listRolesGlobal(Map<String, String> filters, Pageable pageable) {
        return toSummaryPage(roleRepository.findAllGlobal(filters, pageable));
    }

    private Page<RoleSummaryResponse> toSummaryPage(Page<Role> page) {
        List<UUID> ids = page.getContent().stream().map(Role::getId).toList();
        Map<UUID, Long> counts = roleRepository.countPermissionsByIds(ids);

        return page.map(role -> new RoleSummaryResponse(
                role.getId(),
                role.getName(),
                role.getDisplayName(),
                role.getDescription(),
                role.getIsSystemDefault() != null ? role.getIsSystemDefault() : false,
                counts.getOrDefault(role.getId(), 0L)
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse findRoleById(UUID roleId, UUID currentTenant) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("auth.role.not_found"));
        enforceTenantAccess(role, currentTenant);
        return toRoleResponse(role);
    }

    private RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getDisplayName(),
                role.getIsSystemDefault() != null ? role.getIsSystemDefault() : false,
                role.getPermissions() != null ? role.getPermissions().stream()
                        .map(p -> new PermissionResponse(p.getId(), p.getName(), p.getDescription()))
                        .collect(Collectors.toSet()) : Set.of()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> listAvailablePermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionResponse(p.getId(), p.getName(), p.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    @Auditable
    @Transactional
    public void updateRole(UUID roleId, UUID currentTenant, RoleUpdateRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("auth.role.not_found"));

        enforceTenantAccess(role, currentTenant);

        role.setDisplayName(request.displayName());
        role.setDescription(request.description());
        role.setPermissions(new HashSet<>(
                permissionRepository.findAllById(request.permissionIds())));

        roleRepository.save(role);
    }

    @Override
    @Auditable
    @Transactional
    public void deleteRole(UUID roleId, UUID currentTenant) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("auth.role.not_found"));
        enforceTenantAccess(role, currentTenant);
        roleRepository.deleteById(roleId, currentTenant);
    }

    @Override
    @Auditable
    @Transactional
    public void deleteRolesBatch(List<UUID> roleIds, UUID currentTenant) {
        boolean isGlobalAdmin = SystemConstants.SYSTEM_TENANT_ID.equals(currentTenant);
        for (UUID id : roleIds) {
            roleRepository.findById(id)
                    .ifPresent(role -> enforceTenantAccess(role, currentTenant));
        }
        roleRepository.deleteAllIdInBatch(roleIds, currentTenant, isGlobalAdmin);
    }

    @Override
    @Transactional
    public void assignRoleToUser(UUID userId, UUID roleId) {
        userRepository.addRoleToUser(userId, roleId);
    }

    @Override
    @Transactional
    public void createPermission(String name, String description) {
        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setName(name);
        permission.setDescription(description);
        permissionRepository.save(permission);
    }

    private void enforceTenantAccess(Role role, UUID currentTenant) {
        boolean isSystemRole = Boolean.TRUE.equals(role.getIsSystemDefault());
        boolean isGlobalAdmin = SystemConstants.SYSTEM_TENANT_ID.equals(currentTenant);

        if (isSystemRole && !isGlobalAdmin) {
            throw new BusinessException("auth.role.system_locked",
                    "Perfis do sistema só podem ser alterados pelo administrador geral.");
        }
        if (!isSystemRole
                && role.getTenantId() != null
                && !role.getTenantId().equals(currentTenant)
                && !isGlobalAdmin) {
            throw new BusinessException("auth.role.forbidden",
                    "Você não tem permissão para alterar este perfil.");
        }
    }
}