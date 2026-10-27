package com.smart_extract_service.modules.auth.domain.ports.output;

import com.smart_extract_service.modules.auth.domain.model.Permission;

import java.util.List;
import java.util.UUID;

public interface PermissionRepositoryPort {
    List<Permission> findAllById(List<UUID> ids);
    List<Permission> findAll();
    Permission save(Permission permission);
}