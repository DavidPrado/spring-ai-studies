package com.smart_extract_service.modules.auth.application.dto;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String name,
        String description
){}
