package com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_login_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "successful", nullable = false)
    private Boolean successful;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    public static UserLoginHistoryEntity createRegistrationHistory(UserEntity user, String ipAddress, String userAgent) {
        return UserLoginHistoryEntity.builder()
                .user(user)
                .loginAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .successful(true)
                .failureReason("User registration")
                .build();
    }
}