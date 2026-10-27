package com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities;

import com.smart_extract_service.shared.audit.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity extends BaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "phone_number2", length = 20)
    private String phoneNumber2;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "cost_center", length = 50)
    private String costCenter;

    @Column(name = "employee_code", length = 50)
    private String employeeCode;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "require_password_change")
    private Boolean requirePasswordChange = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_password_change_at")
    private LocalDateTime lastPasswordChangeAt;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @OneToOne(mappedBy = "user",
            cascade = CascadeType.MERGE,
            fetch = FetchType.LAZY)
    private UserProfilePictureEntity profilePicture;

    @Column(name = "is_temporary", nullable = false)
    private boolean isTemporary = false;

    @Transient
    private LocalDateTime accessExpirationDate;

    @Transient
    private Integer passwordChangeIntervalDays = 90;

    @Transient
    private boolean mustChangePasswordNextLogin = false;

    public boolean isAccessExpired() {
        return this.isTemporary && this.accessExpirationDate != null && LocalDateTime.now().isAfter(this.accessExpirationDate);
    }

    public boolean isPasswordExpired() {
        LocalDateTime lastChange = this.lastPasswordChangeAt != null ? this.lastPasswordChangeAt : getCreatedAt();
        if (this.passwordChangeIntervalDays == null || this.passwordChangeIntervalDays <= 0 || lastChange == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(lastChange.plusDays(this.passwordChangeIntervalDays));
    }
}