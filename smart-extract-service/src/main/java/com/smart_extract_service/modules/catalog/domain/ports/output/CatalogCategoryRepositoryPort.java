package com.smart_extract_service.modules.catalog.domain.ports.output;

import com.smart_extract_service.modules.catalog.domain.model.CatalogCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface CatalogCategoryRepositoryPort {
    CatalogCategory save(CatalogCategory category);
    Optional<CatalogCategory> findById(UUID id);
    List<CatalogCategory> findByIds(Collection<UUID> ids);
    List<CatalogCategory> findTree(UUID tenantId, boolean activeOnly);
    Page<CatalogCategory> findAll(UUID tenantId, Map<String, String> filters, Pageable pageable);
    void deleteById(UUID id);
    boolean hasChildren(UUID categoryId);
}
