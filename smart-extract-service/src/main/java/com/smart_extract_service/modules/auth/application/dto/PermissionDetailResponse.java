package com.smart_extract_service.modules.auth.application.dto;

import java.util.UUID;

public record PermissionDetailResponse(
        UUID id,
        String name,
        String description,
        String category
) {}