package com.smart_extract_service.modules.catalog.application.dto;

import java.math.BigDecimal;

public record CatalogItemExportRow(
        String sku,
        String ean,
        String name,
        String categoryName,
        String itemType,
        String status,
        String unitOfMeasure,
        BigDecimal salePrice,
        BigDecimal costPrice
) {
}
