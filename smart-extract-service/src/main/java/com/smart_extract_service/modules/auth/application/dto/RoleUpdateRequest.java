package com.smart_extract_service.modules.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record RoleUpdateRequest(
        @NotBlank(message = "O nome de exibição é obrigatório")
        String displayName,
        String description,
        @NotEmpty(message = "Selecione ao menos uma permissão")
        List<UUID> permissionIds
) {}