package com.smart_extract_service.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginHistory {

    private UUID id;
    private User user;
    private LocalDateTime loginAt;
    private String ipAddress;
    private String userAgent;
    private Boolean successful;
    private String failureReason;

    // Regra de fábrica no domínio
    public static UserLoginHistory createRegistrationHistory(User user, String ipAddress, String userAgent) {
        return UserLoginHistory.builder()
                .id(null)
                .user(user)
                .loginAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .successful(true)
                .failureReason("User registration")
                .build();
    }

    // Helper caso seu código precise apenas verificar se o login foi com sucesso
    public boolean isSuccessful() {
        return Boolean.TRUE.equals(this.successful);
    }
}