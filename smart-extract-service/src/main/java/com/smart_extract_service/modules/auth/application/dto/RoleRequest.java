package com.smart_extract_service.modules.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record RoleRequest(
        @NotBlank(message = "O nome técnico é obrigatório")
        String name,
        String displayName,
        String description,
        @NotEmpty(message = "Selecione ao menos uma permissão")
        List<UUID> permissionIds
) {}
