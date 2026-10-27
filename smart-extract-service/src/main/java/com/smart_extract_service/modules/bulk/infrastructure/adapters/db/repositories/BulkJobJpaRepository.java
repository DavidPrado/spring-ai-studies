package com.smart_extract_service.modules.bulk.infrastructure.adapters.db.repositories;

import com.smart_extract_service.modules.bulk.domain.enums.BulkJobStatus;
import com.smart_extract_service.modules.bulk.infrastructure.adapters.db.entities.BulkJobEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BulkJobJpaRepository extends JpaRepository<BulkJobEntity, UUID> {

    List<BulkJobEntity> findTop20ByTenantIdAndUserIdOrderByCreatedAtDesc(
            UUID tenantId, UUID userId);

    @Query("SELECT j FROM BulkJobEntity j WHERE j.status = :status ORDER BY j.queuedAt ASC")
    List<BulkJobEntity> findTopNByStatusOrderByQueuedAtAsc(
            @Param("status") BulkJobStatus status,
            Pageable pageable);

    @Modifying
    @Query("UPDATE BulkJobEntity j SET j.status = :status WHERE j.id = :jobId")
    int updateStatus(@Param("jobId") UUID jobId, @Param("status") BulkJobStatus status);

    @Modifying
    @Query("UPDATE BulkJobEntity j SET j.processedItems = :processed, " +
            "j.successItems = :success, j.errorItems = :error WHERE j.id = :jobId")
    int updateProgress(
            @Param("jobId") UUID jobId,
            @Param("processed") int processedItems,
            @Param("success") int successItems,
            @Param("error") int errorItems);

    @Modifying
    @Query("UPDATE BulkJobEntity j SET j.status = 'FAILED', " +
            "j.lastError = :error, j.finishedAt = CURRENT_TIMESTAMP, " +
            "j.retryCount = j.retryCount + 1 WHERE j.id = :jobId")
    int markFailed(@Param("jobId") UUID jobId, @Param("error") String errorMessage);
}
