package com.smart_extract_service.modules.catalog.application.dto;

import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import com.smart_extract_service.modules.catalog.domain.enums.ItemType;
import com.smart_extract_service.modules.catalog.domain.enums.UnitOfMeasure;
import com.smart_extract_service.modules.catalog.domain.enums.Vertical;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CatalogItemResponse(
        UUID id,
        String sku,
        String ean,
        String name,
        String description,
        ItemType itemType,
        ItemStatus status,
        UUID categoryId,
        String categoryName,
        Vertical vertical,
        UnitOfMeasure unitOfMeasure,
        BigDecimal salePrice,
        BigDecimal costPrice,
        BigDecimal marginPercent,
        String ncm, String cest, String taxOrigin, String cstIcms, String csosn,
        String serviceCode, BigDecimal issRate,
        boolean tracksBatch, boolean tracksExpiry, boolean isHazardous,
        Map<String, Object> extraData,
        Integer minStock, Integer maxStock, Integer reorderStock,
        List<VariantResponse> variants,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}