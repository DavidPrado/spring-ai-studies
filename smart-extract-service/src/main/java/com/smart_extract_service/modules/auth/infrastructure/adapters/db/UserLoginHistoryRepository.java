package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserLoginHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistoryEntity, UUID> {

    Optional<UserLoginHistoryEntity> findFirstByUserIdAndSuccessfulFalseOrderByLoginAtDesc(UUID userId);

    @Query("SELECT COUNT(h) FROM UserLoginHistoryEntity h WHERE h.user.id = :userId AND h.successful = false AND h.loginAt >= :since")
    long countFailedAttemptsSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}