package com.smart_extract_service.modules.catalog.application.dto;

import com.smart_extract_service.modules.catalog.domain.enums.Vertical;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        UUID parentId,
        String name,
        Vertical vertical,
        boolean active,
        long itemCount
) {
}
