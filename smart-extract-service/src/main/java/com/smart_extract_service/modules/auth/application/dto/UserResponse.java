package com.smart_extract_service.modules.auth.application.dto;

import com.smart_extract_service.modules.auth.domain.model.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String cpf,
        String phone,
        String phoneNumber2,
        String jobTitle,
        String department,
        String costCenter,
        String employeeCode,
        String avatarUrl,
        String status,
        String additionalInfo,
        Boolean active,
        Boolean emailVerified,
        Boolean requirePasswordChange,
        Boolean isTemporary,
        LocalDateTime expiresAt,
        Integer passwordChangeIntervalDays,
        LocalDateTime createdAt,
        Integer loginAttempts,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt,
        String language,
        boolean hasProfilePicture,
        TenantResponse tenant,
        Set<RoleResponse> roles
) {
    public static UserResponse fromDomain(User user, boolean hasProfilePicture, TenantResponse tenant, Set<RoleResponse> roles) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                null,
                user.getPhoneNumber(),
                user.getPhoneNumber2(),
                user.getPosition(),
                user.getDepartment(),
                user.getCostCenter(),
                user.getEmployeeCode(),
                null,
                null,
                null,
                user.getActive(),
                user.getEmailVerified(),
                user.getRequirePasswordChange(),
                user.getIsTemporary(),
                user.getExpiresAt(),
                user.getPasswordChangeIntervalDays(),
                user.getCreatedAt(),
                user.getFailedLoginAttempts(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                "pt-BR",
                hasProfilePicture,
                tenant,
                roles
        );
    }
}