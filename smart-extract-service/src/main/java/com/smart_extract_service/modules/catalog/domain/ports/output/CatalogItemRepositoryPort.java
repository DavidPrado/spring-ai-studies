package com.smart_extract_service.modules.catalog.domain.ports.output;

import com.smart_extract_service.modules.catalog.application.dto.CatalogItemExportRow;
import com.smart_extract_service.modules.catalog.domain.enums.ItemType;
import com.smart_extract_service.modules.catalog.domain.model.CatalogItem;
import com.smart_extract_service.modules.catalog.domain.model.CatalogItemVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.function.Consumer;

public interface CatalogItemRepositoryPort {
    CatalogItem save(CatalogItem item);

    Optional<CatalogItem> findById(UUID id);

    Page<CatalogItem> findAll(UUID tenantId, Map<String, String> filters, Pageable pageable);

    boolean existsSku(UUID tenantId, String sku, UUID exceptId);

    boolean existsEan(UUID tenantId, String ean, UUID exceptId);

    Map<UUID, Long> countActiveByCategoryIds(Collection<UUID> categoryIds);

    String nextSku(UUID tenantId, ItemType type);

    CatalogItemVariant saveVariant(CatalogItemVariant variant);

    List<CatalogItemVariant> findVariants(UUID itemId);

    void deleteVariantsByItem(UUID itemId);

    void updateEmbedding(UUID itemId, float[] embedding);

    boolean existsVariantSku(UUID tenantId, String sku, UUID exceptVariantId);

    long countByFilters(UUID tenantId, Map<String, String> filters);

    void streamForExport(UUID tenantId, Map<String, String> filters, Consumer<CatalogItemExportRow> sink);

    List<CatalogItem> findBySemanticSimilarity(UUID tenantId, float[] queryEmbedding, int topK, double similarityThreshold);
}
