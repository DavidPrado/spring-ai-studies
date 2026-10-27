package com.smart_extract_service.modules.catalog.application.dto;

import com.smart_extract_service.modules.catalog.domain.enums.Vertical;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CategoryRequest(
        @NotBlank(message = "Informe o nome da seção")
        String name,
        UUID parentId,
        @NotNull(message = "Informe a familia de négocio da seção")
        Vertical vertical,
        Boolean active
) {
}
