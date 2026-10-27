package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities;

import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import com.smart_extract_service.modules.catalog.domain.enums.ItemType;
import com.smart_extract_service.modules.catalog.domain.enums.UnitOfMeasure;
import com.smart_extract_service.shared.audit.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "catalog_items")
@Getter
@Setter
public class CatalogItemEntity extends BaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 10)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ItemStatus status;

    @Column(name = "sku", nullable = false, length = 40)
    private String sku;

    @Column(name = "ean", length = 14)
    private String ean;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false, length = 5)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;

    @Column(name = "cost_price", nullable = false)
    private BigDecimal costPrice;

    @Column(name = "ncm", length = 10)
    private String ncm;

    @Column(name = "cest", length = 10)
    private String cest;

    @Column(name = "tax_origin", length = 2)
    private String taxOrigin;

    @Column(name = "cst_icms", length = 3)
    private String cstIcms;

    @Column(name = "csosn", length = 3)
    private String csosn;

    @Column(name = "service_code", length = 10)
    private String serviceCode;

    @Column(name = "iss_rate")
    private BigDecimal issRate;

    @Column(name = "tracks_batch", nullable = false)
    private boolean tracksBatch;

    @Column(name = "tracks_expiry", nullable = false)
    private boolean tracksExpiry;

    @Column(name = "is_hazardous", nullable = false)
    private boolean isHazardous;

    /** JSONB: respostas dos campos extras da categoria. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_data", nullable = false)
    private Map<String, Object> extraData = new HashMap<>();

    @Column(name = "min_stock")
    private Integer minStock;

    @Column(name = "max_stock")
    private Integer maxStock;

    @Column(name = "reorder_stock")
    private Integer reorderStock;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}