package com.smart_extract_service.modules.auth.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserPasswordHistory {

    private UUID id;
    private UUID userId;
    private String passwordHash;
    private LocalDateTime createdAt;

    public UserPasswordHistory(UUID id, UUID userId, String passwordHash, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public static UserPasswordHistory create(UUID userId, String passwordHash) {
        return new UserPasswordHistory(UUID.randomUUID(), userId, passwordHash, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
