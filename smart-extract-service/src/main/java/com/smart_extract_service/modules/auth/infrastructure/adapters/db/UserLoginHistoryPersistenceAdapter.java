package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.modules.auth.domain.model.User;
import com.smart_extract_service.modules.auth.domain.model.UserLoginHistory;
import com.smart_extract_service.modules.auth.domain.ports.output.UserLoginHistoryRepositoryPort;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserLoginHistoryEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.mappers.UserLoginHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserLoginHistoryPersistenceAdapter implements UserLoginHistoryRepositoryPort {

    private final UserLoginHistoryRepository repository;

    @Override
    public UserLoginHistory save(UserLoginHistory history) {
        UserLoginHistoryEntity entity = UserLoginHistoryMapper.toEntity(history);

        entity.setId(null);

        UserLoginHistoryEntity savedEntity = repository.save(entity);
        return UserLoginHistoryMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<UserLoginHistory> findLastFailedAttemptByUser(User user) {
        if (user == null || user.getId() == null) return Optional.empty();

        return repository.findFirstByUserIdAndSuccessfulFalseOrderByLoginAtDesc(user.getId())
                .map(UserLoginHistoryMapper::toDomain);
    }

    @Override
    public long countFailedAttemptsLastHour(User user) {
        if (user == null || user.getId() == null) return 0L;

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return repository.countFailedAttemptsSince(user.getId(), oneHourAgo);
    }
}