package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.mappers;


import com.smart_extract_service.modules.catalog.domain.model.AttributeDef;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities.AttributeDefEntity;

import java.time.Instant;

public final class AttributeDefMapper {

    private AttributeDefMapper() {}

    public static AttributeDef toDomain(AttributeDefEntity e) {
        AttributeDef d = new AttributeDef();
        d.setId(e.getId());
        d.setTenantId(e.getTenantId());
        d.setCategoryId(e.getCategoryId());
        d.setKey(e.getAttrKey());
        d.setLabel(e.getLabel());
        d.setDataType(e.getDataType());
        d.setUnit(e.getUnit());
        d.setOptions(e.getOptions());
        d.setRequired(e.isRequired());
        d.setAppliesTo(e.getAppliesTo());
        d.setSortOrder(e.getSortOrder());
        d.setCreatedBy(e.getCreatedBy());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    public static AttributeDefEntity toEntity(AttributeDef d) {
        AttributeDefEntity e = new AttributeDefEntity();
        e.setId(d.getId());
        e.setTenantId(d.getTenantId());
        e.setCategoryId(d.getCategoryId());
        e.setAttrKey(d.getKey());
        e.setLabel(d.getLabel());
        e.setDataType(d.getDataType());
        e.setUnit(d.getUnit());
        e.setOptions(d.getOptions());
        e.setRequired(d.isRequired());
        e.setAppliesTo(d.getAppliesTo());
        e.setSortOrder(d.getSortOrder());
        return e;
    }
}
