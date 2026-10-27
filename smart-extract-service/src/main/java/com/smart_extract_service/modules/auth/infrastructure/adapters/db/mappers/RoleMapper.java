package com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers;

import com.smart_extract_service.modules.auth.domain.model.Role;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.RoleEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.TenantEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class RoleMapper {

    public static Role toDomain(RoleEntity entity) {
        if (entity == null) return null;

        return new Role(
                entity.getId(),
                entity.getTenant() != null ? entity.getTenant().getId() : null,
                entity.getName(),
                entity.getDisplayName(),
                entity.getDescription(),
                entity.getIsSystemDefault(),
                entity.getIsEditable(),
                entity.getPermissions() != null
                        ? entity.getPermissions().stream()
                        .map(PermissionMapper::toDomain)
                        .collect(Collectors.toSet())
                        : Set.of()
        );
    }

    public static RoleEntity toEntity(Role domain) {
        if (domain == null) return null;

        RoleEntity entity = new RoleEntity();
        entity.setId(domain.getId());

        if (domain.getTenantId() != null) {
            TenantEntity tenant = new TenantEntity();
            tenant.setId(domain.getTenantId());
            entity.setTenant(tenant);
        }

        entity.setName(domain.getName());
        entity.setDisplayName(domain.getDisplayName());
        entity.setDescription(domain.getDescription());
        entity.setIsSystemDefault(domain.getIsSystemDefault());
        entity.setIsEditable(domain.getIsEditable());

        if (domain.getPermissions() != null) {
            entity.setPermissions(
                    domain.getPermissions().stream()
                            .map(PermissionMapper::toEntity)
                            .collect(Collectors.toSet())
            );
        }

        return entity;
    }
}