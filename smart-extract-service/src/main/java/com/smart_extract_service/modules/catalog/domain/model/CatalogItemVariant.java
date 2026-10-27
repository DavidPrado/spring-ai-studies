package com.smart_extract_service.modules.catalog.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CatalogItemVariant {

    private UUID id;
    private UUID tenantId;
    private UUID itemId;
    private String sku;
    private String ean;
    private Map<String, Object> extraData = new HashMap<>();
    private BigDecimal priceAdjustment;
    private boolean active;

    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;
}
