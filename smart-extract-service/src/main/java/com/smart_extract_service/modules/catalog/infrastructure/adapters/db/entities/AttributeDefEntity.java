package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities;


import com.smart_extract_service.modules.catalog.domain.enums.AppliesTo;
import com.smart_extract_service.modules.catalog.domain.enums.DataType;
import com.smart_extract_service.shared.audit.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "catalog_attribute_defs")
@Getter
@Setter
public class AttributeDefEntity extends BaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    /** Coluna "key" no banco (palavra reservada em Java → nome de campo diferente). */
    @Column(name = "key", nullable = false, length = 60)
    private String attrKey;

    @Column(name = "label", nullable = false, length = 120)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private DataType dataType;

    @Column(name = "unit", length = 10)
    private String unit;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options")
    private List<String> options;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to", nullable = false, length = 10)
    private AppliesTo appliesTo;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
