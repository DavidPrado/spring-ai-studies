package com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities;

import com.smart_extract_service.shared.audit.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
@Setter
public class TenantEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "subdomain", unique = true)
    private String subdomain;

    @Column(name = "country", length = 3)
    private String country;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "inactivation_reason")
    private String inactivationReason;
}
