package com.smart_extract_service.modules.catalog.infrastructure.adapters.db;

import com.smart_extract_service.modules.catalog.domain.model.AttributeDef;
import com.smart_extract_service.modules.catalog.domain.ports.output.AttributeDefRepositoryPort;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.mappers.AttributeDefMapper;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.repositories.AttributeDefJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttributeDefPersistenceAdapter implements AttributeDefRepositoryPort {

    private final AttributeDefJpaRepository repository;

    @Override
    public AttributeDef save(AttributeDef def) {
        return AttributeDefMapper.toDomain(repository.save(AttributeDefMapper.toEntity(def)));
    }

    @Override
    public Optional<AttributeDef> findById(UUID id) {
        return repository.findById(id).map(AttributeDefMapper::toDomain);
    }

    @Override
    public List<AttributeDef> findByCategory(UUID categoryId) {
        return repository.findByCategoryIdOrderBySortOrderAsc(categoryId).stream()
                .map(AttributeDefMapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsKey(UUID categoryId, String key, UUID exceptId) {
        return repository.existsByCategoryIdAndAttrKeyAndIdNot(categoryId, key,
                exceptId != null ? exceptId : UUID.randomUUID());
    }
}
