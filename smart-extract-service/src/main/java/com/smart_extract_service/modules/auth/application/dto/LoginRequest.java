package com.smart_extract_service.modules.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        String ipAddress,
        String userAgent) {

    public LoginRequest(String email, String password) {
        this(email, password, null, null);
    }
}
