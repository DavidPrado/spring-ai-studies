package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.modules.auth.domain.model.Role;
import com.smart_extract_service.modules.auth.domain.ports.output.RoleRepositoryPort;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.RoleEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers.RoleMapper;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.specs.RoleSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolePersistenceAdapter implements RoleRepositoryPort {

    private final RoleRepository roleRepository;

    @Override
    public Role save(Role role) {
        RoleEntity entity = RoleMapper.toEntity(role);
        RoleEntity savedEntity = roleRepository.save(entity);
        return RoleMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return roleRepository.findById(id)
                .map(RoleMapper::toDomain);
    }

    @Override
    public Page<Role> findAll(UUID tenantId, Map<String, String> filters, Pageable pageable) {
        var spec = RoleSpecification.filterBy(tenantId, filters);
        return roleRepository.findAll(spec, pageable)
                .map(RoleMapper::toDomain);
    }

    @Override
    public void deleteById(UUID roleId, UUID currentTenant) {
        roleRepository.deleteByIdAndTenantId(roleId, currentTenant);
    }

    @Override
    public void deleteAllIdInBatch(List<UUID> roleIds, UUID currentTenant, boolean isGlobalAdmin) {
        if (isGlobalAdmin) {
            roleRepository.deleteAllById(roleIds);                       // admin geral: qualquer role
        } else {
            roleRepository.deleteAllByIdInAndTenantId(roleIds, currentTenant); // tenant: só as próprias
        }
    }

    @Override
    public Optional<Role> findSystemDefaultRole() {
        return roleRepository.findByIsSystemDefaultTrue()
                .map(RoleMapper::toDomain);
    }

    @Override
    public Page<Role> findAllGlobal(Map<String, String> filters, Pageable pageable) {
        var spec = RoleSpecification.filterGlobal(filters);
        return roleRepository.findAll(spec, pageable).map(RoleMapper::toDomain);
    }

    @Override
    public Map<UUID, Long> countPermissionsByIds(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return Map.of();
        return roleRepository.countPermissionsByRoleIds(roleIds).stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));
    }
}