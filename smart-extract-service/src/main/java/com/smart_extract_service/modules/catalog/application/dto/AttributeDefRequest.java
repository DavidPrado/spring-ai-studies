package com.smart_extract_service.modules.catalog.application.dto;

import com.smart_extract_service.modules.catalog.domain.enums.AppliesTo;
import com.smart_extract_service.modules.catalog.domain.enums.DataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.util.List;

public record AttributeDefRequest(
        @NotBlank(message = "Informe a chave técnica(ex.: registry_number)")
        String key,
        @NotBlank(message = "Informe o rotulo exibido (ex.: Registro ANVISA")
        String label,
        @NotNull DataType dataType,
        String unit,
        List<String> options,
        Boolean required,
        AppliesTo appliesTo,
        Integer sortOrder
        ) {
}
