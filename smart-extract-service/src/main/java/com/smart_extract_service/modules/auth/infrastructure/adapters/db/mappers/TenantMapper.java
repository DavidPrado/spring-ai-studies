package com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers;

import com.smart_extract_service.modules.auth.domain.model.Tenant;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.TenantEntity;

public class TenantMapper {

    public static Tenant toDomain(TenantEntity entity) {
        if (entity == null) return null;

        return new Tenant(
                entity.getId(),
                entity.getName(),
                entity.getSubdomain(),
                entity.getCountry(),
                entity.getCurrencyCode(),
                entity.isActive(),
                entity.getBlockedAt(),
                entity.getInactivationReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static TenantEntity toEntity(Tenant domain) {
        if (domain == null) return null;

        TenantEntity entity = new TenantEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setSubdomain(domain.getSubdomain());
        entity.setCountry(domain.getCountry());
        entity.setCurrencyCode(domain.getCurrencyCode());
        if (domain.getActive() != null) {
            entity.setActive(domain.getActive());
        }
        entity.setBlockedAt(domain.getBlockedAt());
        entity.setInactivationReason(domain.getInactivationReason());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        return entity;
    }
}