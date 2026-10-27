package com.smart_extract_service.modules.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserUpdateRequest(
        @Size(max = 200, message = "Nome completo não pode exceder 200 caracteres")
        String fullName,
        @Email(message = "Email deve ser válido")
        @Size(max = 255, message = "Email não pode exceder 255 caracteres")
        String email,
        @Size(max = 20, message = "Telefone não pode exceder 20 caracteres")
        String phoneNumber,
        @Size(max = 20, message = "Telefone secundário não pode exceder 20 caracteres")
        String phoneNumber2,
        @Size(max = 100, message = "Departamento não pode exceder 100 caracteres")
        String department,
        @Size(max = 100, message = "Cargo não pode exceder 100 caracteres")
        String position,
        @Size(max = 50, message = "Centro de custo não pode exceder 50 caracteres")
        String costCenter,
        @Size(max = 50, message = "Código do colaborador não pode exceder 50 caracteres")
        String employeeCode,
        Boolean active,
        Boolean requirePasswordChange,
        Boolean isTemporary,
        LocalDateTime expiresAt,
        Integer passwordChangeIntervalDays,
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String password,
        UUID tenantId,
        Set<UUID> roleIds
) {
}
