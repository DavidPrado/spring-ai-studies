package com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers;

import com.smart_extract_service.modules.auth.domain.model.Permission;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.PermissionEntity;

public class PermissionMapper {

    public static Permission toDomain(PermissionEntity entity) {
        if (entity == null) return null;
        Permission domain = new Permission();
        domain.setId(entity.getId());
        domain.setName(entity.getName());
        domain.setDescription(entity.getDescription());
        domain.setCategory(entity.getCategory());   // ← ADICIONAR
        return domain;
    }

    public static PermissionEntity toEntity(Permission domain) {
        if (domain == null) return null;
        return PermissionEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .category(domain.getCategory())     // ← ADICIONAR
                .build();
    }
}