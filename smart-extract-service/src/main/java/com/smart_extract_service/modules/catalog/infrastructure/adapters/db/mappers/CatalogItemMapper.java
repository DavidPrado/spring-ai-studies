package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.mappers;

import com.smart_extract_service.modules.catalog.domain.model.CatalogItem;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities.CatalogItemEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;

public final class CatalogItemMapper {

    private CatalogItemMapper() {}

    public static CatalogItem toDomain(CatalogItemEntity e) {
        CatalogItem d = new CatalogItem();
        d.setId(e.getId());
        d.setTenantId(e.getTenantId());
        d.setCategoryId(e.getCategoryId());
        d.setItemType(e.getItemType());
        d.setStatus(e.getStatus());
        d.setSku(e.getSku());
        d.setEan(e.getEan());
        d.setName(e.getName());
        d.setDescription(e.getDescription());
        d.setUnitOfMeasure(e.getUnitOfMeasure());
        d.setSalePrice(e.getSalePrice());
        d.setCostPrice(e.getCostPrice());
        d.setNcm(e.getNcm());
        d.setCest(e.getCest());
        d.setTaxOrigin(e.getTaxOrigin());
        d.setCstIcms(e.getCstIcms());
        d.setCsosn(e.getCsosn());
        d.setServiceCode(e.getServiceCode());
        d.setIssRate(e.getIssRate());
        d.setTracksBatch(e.isTracksBatch());
        d.setTracksExpiry(e.isTracksExpiry());
        d.setHazardous(e.isHazardous());
        d.setExtraData(e.getExtraData() != null ? new HashMap<>(e.getExtraData()) : new HashMap<>());
        d.setMinStock(e.getMinStock());
        d.setMaxStock(e.getMaxStock());
        d.setReorderStock(e.getReorderStock());
        d.setDeletedAt(e.getDeletedAt());
        d.setCreatedAt(e.getCreatedAt());
        d.setCreatedBy(e.getCreatedBy());
        d.setUpdatedAt(e.getUpdatedAt());
        d.setUpdatedBy(e.getUpdatedBy());
        return d;
    }

    public static CatalogItemEntity toEntity(CatalogItem d) {
        CatalogItemEntity e = new CatalogItemEntity();
        e.setId(d.getId());
        e.setTenantId(d.getTenantId());
        e.setCategoryId(d.getCategoryId());
        e.setItemType(d.getItemType());
        e.setStatus(d.getStatus());
        e.setSku(d.getSku());
        e.setEan(d.getEan());
        e.setName(d.getName());
        e.setDescription(d.getDescription());
        e.setUnitOfMeasure(d.getUnitOfMeasure());
        e.setSalePrice(d.getSalePrice());
        e.setCostPrice(d.getCostPrice());
        e.setNcm(d.getNcm());
        e.setCest(d.getCest());
        e.setTaxOrigin(d.getTaxOrigin());
        e.setCstIcms(d.getCstIcms());
        e.setCsosn(d.getCsosn());
        e.setServiceCode(d.getServiceCode());
        e.setIssRate(d.getIssRate());
        e.setTracksBatch(d.isTracksBatch());
        e.setTracksExpiry(d.isTracksExpiry());
        e.setHazardous(d.isHazardous());
        e.setExtraData(d.getExtraData() != null ? new HashMap<>(d.getExtraData()) : new HashMap<>());
        e.setMinStock(d.getMinStock());
        e.setMaxStock(d.getMaxStock());
        e.setReorderStock(d.getReorderStock());
        e.setDeletedAt(d.getDeletedAt());
        return e;
    }
}