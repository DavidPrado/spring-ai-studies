package com.smart_extract_service.modules.auth.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String subdomain,
        String country,
        String currencyCode,
        boolean active,
        String inactivationReason,
        LocalDateTime blockedAt
) {}
