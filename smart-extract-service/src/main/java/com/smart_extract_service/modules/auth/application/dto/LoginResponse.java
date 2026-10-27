package com.smart_extract_service.modules.auth.application.dto;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String token,
        String type,
        Long expiresIn,
        String userName,
        List<String> roles,
        UUID tenantId
) {}
