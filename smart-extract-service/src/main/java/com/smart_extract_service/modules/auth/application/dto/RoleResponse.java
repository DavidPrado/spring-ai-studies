package com.smart_extract_service.modules.auth.application.dto;

import java.util.Set;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        String displayName,
        Boolean isSystemDefault,
        Set<PermissionResponse> permissions
) {}
