package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.modules.auth.domain.model.UserPasswordHistory;
import com.smart_extract_service.modules.auth.domain.ports.output.UserPasswordHistoryRepositoryPort;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserPasswordHistoryEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers.UserPasswordHistoryMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserPasswordHistoryPersistenceAdapter implements UserPasswordHistoryRepositoryPort {

    private final UserPasswordHistoryRepository repository;

    public UserPasswordHistoryPersistenceAdapter(UserPasswordHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserPasswordHistory save(UserPasswordHistory history) {
        UserPasswordHistoryEntity entity = UserPasswordHistoryMapper.toEntity(history);
        UserPasswordHistoryEntity savedEntity = repository.save(entity);
        return UserPasswordHistoryMapper.toDomain(savedEntity);
    }

    @Override
    public List<UserPasswordHistory> findTop5ByUserIdOrderByCreatedAtDesc(UUID userId) {
        return repository.findTop5ByUserId(userId, PageRequest.of(0, 5))
                .stream()
                .map(UserPasswordHistoryMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserPasswordHistory> findById(UUID id) {
        return Optional.empty();
    }
}
