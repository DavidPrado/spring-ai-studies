package com.smart_extract_service.modules.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantRequest(
        UUID id,
        @NotBlank(message = "O nome do tenant não deve ser vazio")
        String name,
        @NotBlank(message = "O subdominio não deve ser nulo ou vazio")
        String subdomain,
        @NotBlank(message = "A sigla do pais não deve ser nulo ou vazio")
        @Size(min = 3, max = 3, message = "A sigla do país deve conter exatamente 3 caracteres")
        String country,
        @NotBlank(message = "O código da moeda não deve ser nulo ou vazio")
        @Size(min = 3, max = 3, message = "O código da moeda deve conter exatamente 3 caracteres")
        String currencyCode,
        @NotNull(message = "O status ativo do tenant deve ser informado")
        boolean active,
        LocalDateTime blockedAt,
        String inactivationReason
) {
}
