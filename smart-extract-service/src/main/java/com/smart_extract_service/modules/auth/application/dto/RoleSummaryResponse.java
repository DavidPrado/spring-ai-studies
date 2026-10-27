package com.smart_extract_service.modules.auth.application.dto;

import java.util.UUID;

public record RoleSummaryResponse(
        UUID id,
        String name,
        String displayName,
        String description,
        Boolean isSystemDefault,
        long permissionCount
){}