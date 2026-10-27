package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities;

import com.smart_extract_service.shared.audit.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "catalog_item_variants")
@Getter
@Setter
public class CatalogItemVariantEntity extends BaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "sku", nullable = false, length = 40)
    private String sku;

    @Column(name = "ean", length = 14)
    private String ean;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_data", nullable = false)
    private Map<String, Object> extraData = new HashMap<>();

    @Column(name = "price_adjustment", nullable = false)
    private BigDecimal priceAdjustment;

    @Column(name = "active", nullable = false)
    private boolean active;
}
