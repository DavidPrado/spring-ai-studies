package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.repositories;

import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities.CatalogItemVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CatalogItemVariantJpaRepository extends JpaRepository<CatalogItemVariantEntity, UUID> {
    List<CatalogItemVariantEntity> findByItemIdOrderBySkuAsc(UUID itemId);
    void deleteByItemId(UUID itemId);
    boolean existsByTenantIdAndSku(UUID tenantId, String sku);
    boolean existsByTenantIdAndSkuAndIdNot(UUID tenantId, String sku, UUID id);
}