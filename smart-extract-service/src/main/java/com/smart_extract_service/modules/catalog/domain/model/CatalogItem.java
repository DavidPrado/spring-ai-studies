package com.smart_extract_service.modules.catalog.domain.model;

import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import com.smart_extract_service.modules.catalog.domain.enums.ItemType;
import com.smart_extract_service.modules.catalog.domain.enums.UnitOfMeasure;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CatalogItem {

    private UUID id;
    private UUID tenantId;
    private UUID categoryId;
    private ItemType itemType;
    private ItemStatus status;
    // Identidade
    private String sku;
    private String ean;
    private String name;
    private String description;
    private UnitOfMeasure unitOfMeasure;

    // Comercial
    private BigDecimal salePrice;
    private BigDecimal costPrice;

    // Fiscal mercadoria (BR)
    private String ncm;
    private String cest;
    private String taxOrigin;
    private String cstIcms;
    private String csosn;

    // Fiscal serviço
    private String serviceCode;
    private BigDecimal issRate;

    // Controles verticais
    private boolean tracksBatch;
    private boolean tracksExpiry;
    private boolean hazardous;

    /** Dados extras da categoria (o "formulário preenchido"). */
    private Map<String, Object> extraData = new HashMap<>();

    // Reposição (somente PRODUCT)
    private Integer minStock;
    private Integer maxStock;
    private Integer reorderStock;

    private float[] embedding;

    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    public boolean isService() { return itemType == ItemType.SERVICE; }
    public boolean isProduct() { return itemType == ItemType.PRODUCT; }
    public boolean isDeleted() { return deletedAt != null; }

    /** RN-03: transições permitidas do ciclo de vida. */
    public void changeStatus(ItemStatus target) {
        if (this.status == target) return;
        boolean allowed = switch (this.status) {
            case DRAFT    -> target == ItemStatus.ACTIVE;
            case ACTIVE   -> target == ItemStatus.INACTIVE;
            case INACTIVE -> target == ItemStatus.ACTIVE;
        };
        if (!allowed) {
            throw new BusinessException("catalog.item.invalid_transition",
                    "Não é possível mudar o status de %s para %s.".formatted(this.status, target));
        }
        this.status = target;
    }

    public void activate()   { changeStatus(ItemStatus.ACTIVE); }
    public void deactivate() { changeStatus(ItemStatus.INACTIVE); }

    public void softDelete() { this.deletedAt = LocalDateTime.now(); }

    public void ensureTypeConsistency() {
        if (isService()) {
            this.minStock = null;
            this.maxStock = null;
            this.reorderStock = null;
            this.tracksBatch = false;
            this.tracksExpiry = false;
        }
    }

    public BigDecimal marginPercent() {
        if (salePrice == null || salePrice.signum() <= 0 || costPrice == null) {
            return BigDecimal.ZERO;
        }
        return salePrice.subtract(costPrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(salePrice, 2, RoundingMode.HALF_UP);
    }

}
