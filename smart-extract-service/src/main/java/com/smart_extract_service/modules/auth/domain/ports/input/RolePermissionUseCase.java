package com.smart_extract_service.modules.auth.domain.ports.input;

import com.smart_extract_service.modules.auth.application.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RolePermissionUseCase {
    void createRole(String name, UUID tenantId, RoleRequest request);
    RoleResponse findRoleById(UUID roleId, UUID currentTenant);
    void updateRole(UUID roleId, UUID currentTenant, RoleUpdateRequest request);
    void deleteRole(UUID roleId, UUID currentTenant);
    void deleteRolesBatch(List<UUID> roleIds, UUID currentTenant);
    Page<RoleSummaryResponse> listRoles(UUID tenantId, Map<String, String> filters, Pageable pageable);
    Page<RoleSummaryResponse> listRolesGlobal(Map<String, String> filters, Pageable pageable);
    List<PermissionResponse> listAvailablePermissions();
    void assignRoleToUser(UUID userId, UUID roleId);
    void createPermission(String name, String description);
}