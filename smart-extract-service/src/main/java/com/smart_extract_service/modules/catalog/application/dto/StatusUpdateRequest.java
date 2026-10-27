package com.smart_extract_service.modules.catalog.application.dto;

import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest (
        @NotNull(message = "Informe o novo status (DRAFT, ACTIVE, ou INATIVE)")
        ItemStatus status
) {
}
