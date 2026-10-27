package com.smart_extract_service.modules.auth.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Tenant {

    private UUID id;
    private String name;
    private String subdomain;
    private String country;
    private String currencyCode;
    private Boolean active = true;
    private LocalDateTime blockedAt;
    private String inactivationReason;
    private LocalDateTime createdAt; // 👈 Necessário para receber da BaseEntity
    private LocalDateTime updatedAt; // 👈 Necessário para receber da BaseEntity

    public Tenant() {}

    public Tenant(UUID id, String name, String subdomain, String country, String currencyCode,
                  Boolean active, LocalDateTime blockedAt, String inactivationReason,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.subdomain = subdomain;
        this.country = country;
        this.currencyCode = currencyCode;
        this.active = active;
        this.blockedAt = blockedAt;
        this.inactivationReason = inactivationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Regras de negócio do Tenant
    public void activate() {
        this.active = true;
        this.blockedAt = null;
        this.inactivationReason = null;
    }

    public void block(String reason) {
        this.active = false;
        this.blockedAt = LocalDateTime.now();
        this.inactivationReason = reason;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubdomain() { return subdomain; }
    public void setSubdomain(String subdomain) { this.subdomain = subdomain; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getBlockedAt() { return blockedAt; }
    public void setBlockedAt(LocalDateTime blockedAt) { this.blockedAt = blockedAt; }

    public String getInactivationReason() { return inactivationReason; }
    public void setInactivationReason(String inactivationReason) { this.inactivationReason = inactivationReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}