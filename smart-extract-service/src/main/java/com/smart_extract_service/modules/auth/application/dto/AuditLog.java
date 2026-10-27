package com.smart_extract_service.modules.auth.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLog(
        UUID userId,
        UUID tenantId,
        String action,
        String entity,
        String entityId,
        String oldValue,
        String newValue,
        String ipAddress,
        LocalDateTime timestamp
) {
}
