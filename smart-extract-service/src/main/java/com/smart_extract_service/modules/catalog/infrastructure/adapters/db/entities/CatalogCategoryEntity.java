package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities;

import com.smart_extract_service.modules.catalog.domain.enums.Vertical;
import com.smart_extract_service.shared.audit.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "catalog_categories")
@Getter
@Setter
public class CatalogCategoryEntity extends BaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "vertical", nullable = false, length = 20)
    private Vertical vertical;

    @Column(name = "active", nullable = false)
    private boolean active;
}
