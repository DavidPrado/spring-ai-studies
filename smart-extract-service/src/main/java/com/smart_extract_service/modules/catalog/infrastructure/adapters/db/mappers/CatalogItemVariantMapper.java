package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.mappers;

import com.smart_extract_service.modules.catalog.domain.model.CatalogItemVariant;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities.CatalogItemVariantEntity;

import java.time.Instant;
import java.util.HashMap;

public final class CatalogItemVariantMapper {

    private CatalogItemVariantMapper() {}

    public static CatalogItemVariant toDomain(CatalogItemVariantEntity e) {
        CatalogItemVariant d = new CatalogItemVariant();
        d.setId(e.getId());
        d.setTenantId(e.getTenantId());
        d.setItemId(e.getItemId());
        d.setSku(e.getSku());
        d.setEan(e.getEan());
        d.setExtraData(e.getExtraData() != null ? new HashMap<>(e.getExtraData()) : new HashMap<>());
        d.setPriceAdjustment(e.getPriceAdjustment());
        d.setActive(e.isActive());
        d.setCreatedAt(e.getCreatedAt());
        d.setCreatedBy(e.getCreatedBy());
        d.setUpdatedAt(e.getUpdatedAt());
        d.setUpdatedBy(e.getUpdatedBy());
        return d;
    }

    public static CatalogItemVariantEntity toEntity(CatalogItemVariant d) {
        CatalogItemVariantEntity e = new CatalogItemVariantEntity();
        e.setId(d.getId());
        e.setTenantId(d.getTenantId());
        e.setItemId(d.getItemId());
        e.setSku(d.getSku());
        e.setEan(d.getEan());
        e.setExtraData(d.getExtraData() != null ? new HashMap<>(d.getExtraData()) : new HashMap<>());
        e.setPriceAdjustment(d.getPriceAdjustment());
        e.setActive(d.isActive());
        return e;
    }
}
