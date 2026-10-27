package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.modules.auth.domain.model.Permission;
import com.smart_extract_service.modules.auth.domain.ports.output.PermissionRepositoryPort;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.PermissionEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers.PermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PermissionPersistenceAdapter implements PermissionRepositoryPort {

    private final PermissionRepository permissionRepository;

    @Override
    public List<Permission> findAllById(List<UUID> ids) {
        return permissionRepository.findAllById(ids).stream()
                .map(PermissionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> findAll() {
        return permissionRepository.findAll().stream()
                .map(PermissionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Permission save(Permission permission) {
        PermissionEntity entity = PermissionMapper.toEntity(permission);
        PermissionEntity savedEntity = permissionRepository.save(entity);
        return PermissionMapper.toDomain(savedEntity);
    }
}