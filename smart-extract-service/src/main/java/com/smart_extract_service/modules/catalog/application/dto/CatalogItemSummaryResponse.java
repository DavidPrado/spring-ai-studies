package com.smart_extract_service.modules.catalog.application.dto;

import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import com.smart_extract_service.modules.catalog.domain.enums.ItemType;
import com.smart_extract_service.modules.catalog.domain.enums.UnitOfMeasure;
import com.smart_extract_service.modules.catalog.domain.enums.Vertical;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CatalogItemSummaryResponse(
        UUID id,
        String sku,
        String ean,
        String name,
        ItemType itemType,
        ItemStatus status,
        UUID categoryId,
        String categoryName,
        Vertical vertical,
        UnitOfMeasure unitOfMeasure,
        BigDecimal salePrice,
        BigDecimal costPrice,
        BigDecimal marginPercent,
        boolean tracksBatch,
        boolean tracksExpiry,
        boolean isHazardous,
        LocalDateTime updateAt
) {
}
