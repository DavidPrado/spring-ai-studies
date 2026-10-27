package com.smart_extract_service.modules.catalog.infrastructure.adapters.db;

import com.smart_extract_service.modules.catalog.application.dto.CatalogItemExportRow;
import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import com.smart_extract_service.modules.catalog.domain.enums.ItemType;
import com.smart_extract_service.modules.catalog.domain.model.CatalogItem;
import com.smart_extract_service.modules.catalog.domain.model.CatalogItemVariant;
import com.smart_extract_service.modules.catalog.domain.ports.output.CatalogItemRepositoryPort;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities.CatalogItemEntity;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.mappers.CatalogItemMapper;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.mappers.CatalogItemVariantMapper;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.repositories.CatalogItemJpaRepository;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.repositories.CatalogItemVariantJpaRepository;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.specs.CatalogItemSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.QueryHints;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class CatalogItemPersistenceAdapter implements CatalogItemRepositoryPort {

    private final CatalogItemJpaRepository itemRepository;
    private final CatalogItemVariantJpaRepository variantRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.bulk.fetch-size:500}")
    private int fetchSize;

    @Override
    public CatalogItem save(CatalogItem item) {
        return CatalogItemMapper.toDomain(itemRepository.save(CatalogItemMapper.toEntity(item)));
    }

    @Override
    public Optional<CatalogItem> findById(UUID id) {
        return itemRepository.findById(id).map(CatalogItemMapper::toDomain);
    }

    @Override
    public Page<CatalogItem> findAll(UUID tenantId, Map<String, String> filters, Pageable pageable) {
        return itemRepository.findAll(CatalogItemSpecification.filterBy(tenantId, filters), pageable)
                .map(CatalogItemMapper::toDomain);
    }

    @Override
    public boolean existsSku(UUID tenantId, String sku, UUID exceptId) {
        return exceptId == null
                ? itemRepository.existsByTenantIdAndSku(tenantId, sku)
                : itemRepository.existsByTenantIdAndSkuAndIdNot(tenantId, sku, exceptId);
    }

    @Override
    public boolean existsEan(UUID tenantId, String ean, UUID exceptId) {
        return exceptId == null
                ? itemRepository.existsByTenantIdAndEan(tenantId, ean)
                : itemRepository.existsByTenantIdAndEanAndIdNot(tenantId, ean, exceptId);
    }

    @Override
    public Map<UUID, Long> countActiveByCategoryIds(Collection<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return Map.of();
        return itemRepository.countByStatusGroupByCategory(ItemStatus.ACTIVE, categoryIds).stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));
    }

    @Override
    public String nextSku(UUID tenantId, ItemType type) {
        String prefix = type == ItemType.SERVICE ? "SRV-" : "PRD-";
        long seq = itemRepository.nextSkuSequence(tenantId, prefix);
        return "%s%06d".formatted(prefix, seq);
    }

    @Override
    public CatalogItemVariant saveVariant(CatalogItemVariant variant) {
        return CatalogItemVariantMapper.toDomain(
                variantRepository.save(CatalogItemVariantMapper.toEntity(variant)));
    }

    @Override
    public List<CatalogItemVariant> findVariants(UUID itemId) {
        return variantRepository.findByItemIdOrderBySkuAsc(itemId).stream()
                .map(CatalogItemVariantMapper::toDomain).toList();
    }

    @Override
    public void deleteVariantsByItem(UUID itemId) {
        variantRepository.deleteByItemId(itemId);
    }

    @Override
    public void updateEmbedding(UUID itemId, float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(embedding[i]);
        }
        sb.append(']');
        itemRepository.updateEmbedding(embedding, itemId);
    }

    @Override
    public boolean existsVariantSku(UUID tenantId, String sku, UUID exceptVariantId) {
        if (exceptVariantId == null) {
            return variantRepository.existsByTenantIdAndSku(tenantId, sku);
        }
        return variantRepository.existsByTenantIdAndSkuAndIdNot(tenantId, sku, exceptVariantId);
    }

    @Override
    public long countByFilters(UUID tenantId, Map<String, String> filters) {
        return itemRepository.count(buildSpecification(tenantId, filters)); // mesmo spec da listagem
    }

    @Override
    @Transactional(readOnly = true)
    public void streamForExport(UUID tenantId, Map<String, String> filters,
                                Consumer<CatalogItemExportRow> sink) {
        StringBuilder jpql = new StringBuilder("""
                SELECT i.sku, i.ean, i.name, c.name, i.itemType, i.status,
                       i.unitOfMeasure, i.salePrice, i.costPrice
                FROM CatalogItemEntity i
                LEFT JOIN CatalogCategoryEntity c ON c.id = i.categoryId
                WHERE i.tenantId = :tenant AND i.deletedAt IS NULL
                """);

        String search = filters.get("search");
        if (search != null && !search.isBlank()) {
            jpql.append(" AND (LOWER(i.name) LIKE :search OR LOWER(i.sku) LIKE :search"
                    + " OR LOWER(i.ean) LIKE :search)");
        }
        String status = filters.get("status");
        if (status != null && !status.isBlank()) jpql.append(" AND i.status = :status");
        String type = filters.get("itemType");
        if (type != null && !type.isBlank()) jpql.append(" AND i.itemType = :itemType");
        String category = filters.get("categoryId");
        if (category != null && !category.isBlank()) jpql.append(" AND i.categoryId = :categoryId");

        jpql.append(switch (filters.getOrDefault("sort", "createdAt,desc")) {
            case "createdAt,asc" -> " ORDER BY i.createdAt ASC";
            case "name,asc" -> " ORDER BY i.name ASC";
            case "name,desc" -> " ORDER BY i.name DESC";
            default -> " ORDER BY i.createdAt DESC";
        });

        var query = entityManager.createQuery(jpql.toString(), Object[].class)
                .setParameter("tenant", tenantId)
                .setHint(QueryHints.FETCH_SIZE, fetchSize);   // cursor Postgres (500)

        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search.trim().toLowerCase() + "%");
        }
        if (status != null && !status.isBlank()) query.setParameter("status", ItemStatus.valueOf(status));
        if (type != null && !type.isBlank()) query.setParameter("itemType", ItemType.valueOf(type));
        if (category != null && !category.isBlank()) query.setParameter("categoryId", UUID.fromString(category));

        try (Stream<Object[]> stream = query.getResultStream()) {   // cursor aberto só aqui
            stream.map(row -> new CatalogItemExportRow(
                            (String) row[0], (String) row[1], (String) row[2], (String) row[3],
                            String.valueOf(row[4]), String.valueOf(row[5]), String.valueOf(row[6]),
                            (BigDecimal) row[7], (BigDecimal) row[8]))
                    .forEach(sink);
        }
    }

    private Specification<CatalogItemEntity> buildSpecification(UUID tenantId, Map<String, String> filters) {
        return CatalogItemSpecification.filterBy(tenantId, filters);
    }

    @Override
    public List<CatalogItem> findBySemanticSimilarity(UUID tenantId,
                                                      float[] queryEmbedding,
                                                      int topK,
                                                      double similarityThreshold) {

        String sql = """
            SELECT c.* FROM catalog_items c
            WHERE c.tenant_id = ?1
              AND c.deleted_at IS NULL
              AND (1 - (c.embedding <=> CAST(?2 AS vector))) >= ?3
            ORDER BY c.embedding <=> CAST(?2 AS vector)
            LIMIT ?4
            """;
        Query query = entityManager.createNativeQuery(sql, CatalogItemEntity.class);

        query.setParameter(1, tenantId);
        query.setParameter(2, queryEmbedding);
        query.setParameter(3, similarityThreshold);
        query.setParameter(4, topK);

        @SuppressWarnings("unchecked")
        List<CatalogItemEntity> entities = query.getResultList();

        return entities.stream()
                .map(CatalogItemMapper::toDomain)
                .toList();
    }
}
