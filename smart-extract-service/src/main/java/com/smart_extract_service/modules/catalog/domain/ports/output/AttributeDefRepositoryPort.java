package com.smart_extract_service.modules.catalog.domain.ports.output;

import com.smart_extract_service.modules.catalog.domain.model.AttributeDef;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttributeDefRepositoryPort {
    AttributeDef save(AttributeDef def);
    Optional<AttributeDef> findById(UUID id);
    List<AttributeDef> findByCategory(UUID categoryId);
    void deleteById(UUID id);
    boolean existsKey(UUID categoryId, String key, UUID exceptId);
}
