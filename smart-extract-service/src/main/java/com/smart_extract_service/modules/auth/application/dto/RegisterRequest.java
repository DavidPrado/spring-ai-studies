package com.smart_extract_service.modules.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterRequest(
        @NotBlank(message = "O nome completo é obrigatório") String fullName,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "Formato de e-mail inválido") String email,
        @NotBlank String password,
        @NotNull(message = "O ID do tenant é obrigatório") UUID tenantId,
        String phoneNumber,
        String phoneNumber2,
        String department,
        String position,
        String costCenter,
        String employeeCode,
        Boolean isTemporary,
        LocalDateTime accessExpirationDate,
        Integer passwordChangeIntervalDays,
        Boolean mustChangePasswordNextLogin
) {
}
