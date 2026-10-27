package com.smart_extract_service.modules.auth.domain.ports.output;

import com.smart_extract_service.modules.auth.domain.model.UserPasswordHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPasswordHistoryRepositoryPort {

    UserPasswordHistory save(UserPasswordHistory history);

    List<UserPasswordHistory> findTop5ByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<UserPasswordHistory> findById(UUID id);
}
