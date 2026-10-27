package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.modules.auth.domain.model.Tenant;
import com.smart_extract_service.modules.auth.domain.ports.output.TenantRepositoryPort;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.TenantEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers.TenantMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TenantPersistenceAdapter implements TenantRepositoryPort {

    private final TenantRepository tenantRepository;

    public TenantPersistenceAdapter(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Tenant save(Tenant tenant) {
        TenantEntity entity = TenantMapper.toEntity(tenant);
        TenantEntity savedEntity = tenantRepository.save(entity);
        return TenantMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Tenant> findById(UUID id) {
        return tenantRepository.findById(id)
                .map(TenantMapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return tenantRepository.existsByName(name);
    }

    @Override
    public Page<Tenant> findAll(Pageable pageable) {
        return tenantRepository.findAll(pageable)
                .map(TenantMapper::toDomain);
    }
}