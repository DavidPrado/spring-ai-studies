package com.smart_extract_service.modules.auth.infrastructure.adapters.db;

import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserPasswordHistoryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserPasswordHistoryRepository extends JpaRepository<UserPasswordHistoryEntity, UUID> {

    @Query("SELECT h FROM UserPasswordHistoryEntity h WHERE h.userId = :userId ORDER BY h.createdAt DESC")
    List<UserPasswordHistoryEntity> findTop5ByUserId(@Param("userId") UUID userId, Pageable pageable);
}
