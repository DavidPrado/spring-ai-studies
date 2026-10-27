package com.smart_extract_service.modules.auth.domain.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class User {

    private UUID id;
    private UUID tenantId;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phoneNumber;
    private String phoneNumber2;
    private String department;
    private String position;
    private String costCenter;
    private String employeeCode;
    private Boolean active;
    private Boolean emailVerified;
    private Boolean requirePasswordChange;
    private LocalDateTime expiresAt;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastPasswordChangeAt;
    private Boolean isTemporary;
    private Integer passwordChangeIntervalDays;
    private Set<Role> roles;

    public User() {}

    public User(UUID id, UUID tenantId, String email, String passwordHash, String fullName,
                String phoneNumber, String phoneNumber2, String department, String position,
                String costCenter, String employeeCode, Boolean active, Boolean emailVerified,
                Boolean requirePasswordChange, LocalDateTime expiresAt, LocalDateTime lastLoginAt,
                String lastLoginIp, Integer failedLoginAttempts, LocalDateTime lockedUntil,
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastPasswordChangeAt,
                Boolean isTemporary, Integer passwordChangeIntervalDays, Set<Role> roles) {
        this.id = id;
        this.tenantId = tenantId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.phoneNumber2 = phoneNumber2;
        this.department = department;
        this.position = position;
        this.costCenter = costCenter;
        this.employeeCode = employeeCode;
        this.active = active;
        this.emailVerified = emailVerified;
        this.requirePasswordChange = requirePasswordChange;
        this.expiresAt = expiresAt;
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastPasswordChangeAt = lastPasswordChangeAt;
        this.isTemporary = isTemporary;
        this.passwordChangeIntervalDays = passwordChangeIntervalDays;
        this.roles = roles;
    }

    // Regras de negócio puras
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.lastPasswordChangeAt = LocalDateTime.now();
        this.requirePasswordChange = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPhoneNumber2() { return phoneNumber2; }
    public void setPhoneNumber2(String phoneNumber2) { this.phoneNumber2 = phoneNumber2; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getCostCenter() { return costCenter; }
    public void setCostCenter(String costCenter) { this.costCenter = costCenter; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Boolean getRequirePasswordChange() { return requirePasswordChange; }
    public void setRequirePasswordChange(Boolean requirePasswordChange) { this.requirePasswordChange = requirePasswordChange; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public String getLastLoginIp() { return lastLoginIp; }
    public void setLastLoginIp(String lastLoginIp) { this.lastLoginIp = lastLoginIp; }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastPasswordChangeAt() { return lastPasswordChangeAt; }
    public void setLastPasswordChangeAt(LocalDateTime lastPasswordChangeAt) { this.lastPasswordChangeAt = lastPasswordChangeAt; }

    public Boolean getIsTemporary() { return isTemporary; }
    public void setIsTemporary(Boolean isTemporary) { this.isTemporary = isTemporary; }

    public Integer getPasswordChangeIntervalDays() { return passwordChangeIntervalDays; }
    public void setPasswordChangeIntervalDays(Integer passwordChangeIntervalDays) { this.passwordChangeIntervalDays = passwordChangeIntervalDays; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}