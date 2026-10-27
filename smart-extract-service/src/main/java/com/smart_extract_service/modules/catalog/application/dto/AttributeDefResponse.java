package com.smart_extract_service.modules.catalog.application.dto;

import com.smart_extract_service.modules.catalog.domain.enums.AppliesTo;
import com.smart_extract_service.modules.catalog.domain.enums.DataType;

import java.util.List;
import java.util.UUID;

public record AttributeDefResponse(
        UUID id,
        UUID categoryId,
        String key,
        String label,
        DataType dataType,
        String unit,
        List<String> options,
        boolean required,
        AppliesTo appliesTo,
        int sortOrder
) {
}
