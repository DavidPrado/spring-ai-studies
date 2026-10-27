package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.repositories;

import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities.AttributeDefEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttributeDefJpaRepository extends JpaRepository<AttributeDefEntity , UUID> {
    List<AttributeDefEntity> findByCategoryIdOrderBySortOrderAsc(UUID categoryId);
    boolean existsByCategoryIdAndAttrKeyAndIdNot(UUID categoryId, String attrKey, UUID id);
}
