package com.smart_extract_service.modules.catalog.application.dto;

import com.smart_extract_service.modules.catalog.domain.enums.ItemType;
import com.smart_extract_service.modules.catalog.domain.enums.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CatalogItemRequest(
        @NotNull(message = "Informe a categoria do item.")
        UUID categoryId,
        @NotNull(message = "Informe se é produto ou serviço")
        ItemType itemType,
        String sku,
        String ean, //Código de barras
        @NotBlank(message = "Informe o nome do item.")
        @Size(min = 3, max = 180, message = "O nome do item deve ter entre 3 e 180 caracteres.")
        String name,
        String description, //Descrição do produto
        @NotNull(message = "Informe a unit de medida do item.")
        UnitOfMeasure unitOfMeasure,
        @NotNull @PositiveOrZero(message = "O preço de venda não pode ser negativo.")
        BigDecimal salePrice,
        @NotNull
        @PositiveOrZero(message = "O preço de custo não pode ser negativo.")
        BigDecimal costPrice,
        String ncm,
        String cest,
        String taxOrigin,
        String cstIcms,
        String csosn,
        String serviceCode,
        BigDecimal issRate,
        boolean tracksBatch,
        boolean tracksExpiry,
        boolean isHazardous,
        Map<String, Object> extraData,
        Integer minStock,
        Integer maxStock,
        Integer reorderStock,
        List<VariantRequest> variants

) {
}
