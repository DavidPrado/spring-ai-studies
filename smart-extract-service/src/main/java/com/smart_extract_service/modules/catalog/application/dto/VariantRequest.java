package com.smart_extract_service.modules.catalog.application.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.Map;

public record VariantRequest(
        @NotBlank(message = "Cada variante precisa de um código próprio")
        String sku,
        String ean,
        Map<String, Object> extraData,
        BigDecimal priceAdjustment,
        Boolean active
) {
}
