package com.smart_extract_service.modules.catalog.application.dto;

import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import com.smart_extract_service.modules.catalog.domain.enums.ItemType;
import com.smart_extract_service.modules.catalog.domain.enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;


public record CatalogItemExportRow(
        // Identidade Básica
        String sku,
        String name,
        String description,
        UUID categoryId,
        ItemType itemType,
        ItemStatus status,
        UnitOfMeasure unitOfMeasure,

        // Comercial
        BigDecimal salePrice,
        BigDecimal costPrice,

        // Fiscal Mercadoria
        String ncm,
        String cest,
        String taxOrigin,
        String cstIcms,
        String csosn,

        // Fiscal Serviço
        String serviceCode,
        BigDecimal issRate,

        // Controles Verticais
        boolean tracksBatch,
        boolean tracksExpiry,
        boolean isHazardous,

        // Estoque
        Integer minStock,
        Integer maxStock,
        Integer reorderStock,

        // Dados Extras
        Map<String, Object> extraData
) {}