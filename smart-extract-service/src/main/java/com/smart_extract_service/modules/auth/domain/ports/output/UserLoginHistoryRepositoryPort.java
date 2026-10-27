package com.smart_extract_service.modules.auth.domain.ports.output;

import com.smart_extract_service.modules.auth.domain.model.User;
import com.smart_extract_service.modules.auth.domain.model.UserLoginHistory;

import java.util.Optional;

public interface UserLoginHistoryRepositoryPort {
    UserLoginHistory save(UserLoginHistory history);
    Optional<UserLoginHistory> findLastFailedAttemptByUser(User user);
    long countFailedAttemptsLastHour(User user);
}