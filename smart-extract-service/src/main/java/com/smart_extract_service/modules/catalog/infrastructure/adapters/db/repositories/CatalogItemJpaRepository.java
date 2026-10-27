package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.repositories;

import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities.CatalogItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CatalogItemJpaRepository extends JpaRepository<CatalogItemEntity, UUID>,
        JpaSpecificationExecutor<CatalogItemEntity> {

    boolean existsByTenantIdAndSku(UUID tenantId, String sku);
    boolean existsByTenantIdAndSkuAndIdNot(UUID tenantId, String sku, UUID id);
    boolean existsByTenantIdAndEan(UUID tenantId, String ean);
    boolean existsByTenantIdAndEanAndIdNot(UUID tenantId, String ean, UUID id);

    @Query("""
           SELECT i.categoryId, COUNT(i)
           FROM CatalogItemEntity i
           WHERE i.status = :status AND i.deletedAt IS NULL AND i.categoryId IN :ids
           GROUP BY i.categoryId
           """)
    List<Object[]> countByStatusGroupByCategory(@Param("status") ItemStatus status,
                                                @Param("ids") Collection<UUID> ids);

    @Query(value = """
           SELECT coalesce(max(substring(i.sku from 5)::int), 0) + 1
           FROM catalog_items i
           WHERE i.tenant_id = :tenantId
             AND i.sku LIKE :prefix || '%'
             AND i.sku ~ ('^' || :prefix || '[0-9]+$')
           """, nativeQuery = true)
    long nextSkuSequence(@Param("tenantId") UUID tenantId, @Param("prefix") String prefix);

    @Modifying
    @Query(value = "UPDATE catalog_items SET embedding = :vec::vector WHERE id = :id", nativeQuery = true)
    void updateEmbedding(@Param("id") UUID id, @Param("vec") String vec);
}
