package com.smart_extract_service.modules.auth.domain.ports.output;

import com.smart_extract_service.modules.auth.domain.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepositoryPort {
    Role save(Role role);
    Optional<Role> findById(UUID id);
    Page<Role> findAll(UUID tenantId, Map<String, String> filters, Pageable pageable);
    Page<Role> findAllGlobal(Map<String, String> filters, Pageable pageable);
    void deleteById(UUID roleId, UUID currentTenant);
    void deleteAllIdInBatch(List<UUID> roleIds, UUID currentTenant, boolean isGlobalAdmin);
    Optional<Role> findSystemDefaultRole();
    Map<UUID, Long> countPermissionsByIds(List<UUID> roleIds);
}