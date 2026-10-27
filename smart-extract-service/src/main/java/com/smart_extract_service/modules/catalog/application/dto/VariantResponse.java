package com.smart_extract_service.modules.catalog.application.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record VariantResponse(
        UUID id,
        String sku,
        String ean,
        Map<String, Object> extraData,
        BigDecimal priceAdjustment,
        boolean active
) {
}
