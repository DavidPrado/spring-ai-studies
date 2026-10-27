package com.smart_extract_service.modules.catalog.infrastructure.adapters.db;

import com.smart_extract_service.modules.catalog.domain.model.CatalogCategory;
import com.smart_extract_service.modules.catalog.domain.ports.output.CatalogCategoryRepositoryPort;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.mappers.CatalogCategoryMapper;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.repositories.CatalogCategoryJpaRepository;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.specs.CatalogCategorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class CatalogCategoryPersistenceAdapter implements CatalogCategoryRepositoryPort {

    private final CatalogCategoryJpaRepository repository;

    @Override
    public CatalogCategory save(CatalogCategory category) {
        return CatalogCategoryMapper.toDomain(
                repository.save(CatalogCategoryMapper.toEntity(category)));
    }

    @Override
    public Optional<CatalogCategory> findById(UUID id) {
        return repository.findById(id).map(CatalogCategoryMapper::toDomain);
    }

    @Override
    public List<CatalogCategory> findByIds(Collection<UUID> ids) {
        if(ids == null || ids.isEmpty()) return List.of();
        return repository.findByIdIn(ids).stream().map(CatalogCategoryMapper::toDomain).toList();
    }

    @Override
    public List<CatalogCategory> findTree(UUID tenantId, boolean activeOnly) {
        return (activeOnly ? repository.findByTenantIdAndActiveTrueOrderByNameAsc(tenantId)
                : repository.findByTenantIdOrderByNameAsc(tenantId))
                .stream().map(CatalogCategoryMapper::toDomain).toList();
    }

    @Override
    public Page<CatalogCategory> findAll(UUID tenantId, Map<String, String> filters, Pageable pageable) {
        return repository.findAll(CatalogCategorySpecification.filterBy(tenantId, filters), pageable)
                .map(CatalogCategoryMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean hasChildren(UUID categoryId) {
        return repository.existsByParentId(categoryId);
    }
}
