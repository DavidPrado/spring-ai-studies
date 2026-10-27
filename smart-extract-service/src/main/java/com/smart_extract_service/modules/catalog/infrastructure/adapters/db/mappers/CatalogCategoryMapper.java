package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.mappers;

import com.smart_extract_service.modules.catalog.domain.model.CatalogCategory;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities.CatalogCategoryEntity;

import java.time.Instant;

public class CatalogCategoryMapper {

    private CatalogCategoryMapper() {}

    public static CatalogCategory toDomain(CatalogCategoryEntity e) {
        CatalogCategory d = new CatalogCategory();
        d.setId(e.getId());
        d.setTenantId(e.getTenantId());
        d.setParentId(e.getParentId());
        d.setName(e.getName());
        d.setVertical(e.getVertical());
        d.setActive(e.isActive());
        d.setCreatedAt(e.getCreatedAt());
        d.setCreatedBy(e.getCreatedBy());
        d.setUpdateAt(e.getUpdatedAt());
        d.setUpdatedBy(e.getUpdatedBy());
        return d;
    }

    public static CatalogCategoryEntity toEntity(CatalogCategory d) {
        CatalogCategoryEntity e = new CatalogCategoryEntity();
        e.setId(d.getId());
        e.setTenantId(d.getTenantId());
        e.setParentId(d.getParentId());
        e.setName(d.getName());
        e.setVertical(d.getVertical());
        e.setActive(d.isActive());
        return e;
    }
}
