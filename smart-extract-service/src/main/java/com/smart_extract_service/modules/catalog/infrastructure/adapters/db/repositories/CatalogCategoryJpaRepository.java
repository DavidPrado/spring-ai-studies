package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.repositories;

import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities.CatalogCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CatalogCategoryJpaRepository extends JpaRepository<CatalogCategoryEntity, UUID>,
        JpaSpecificationExecutor<CatalogCategoryEntity> {

    List<CatalogCategoryEntity> findByTenantIdOrderByNameAsc(UUID tenantId);
    List<CatalogCategoryEntity> findByTenantIdAndActiveTrueOrderByNameAsc(UUID tenantId);
    List<CatalogCategoryEntity> findByIdIn(Collection<UUID> ids);
    boolean existsByParentId(UUID parentId);
}
