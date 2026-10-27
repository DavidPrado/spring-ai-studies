package com.smart_extract_service.modules.bulk.domain.ports.output;

import com.smart_extract_service.modules.bulk.domain.enums.BulkJobStatus;
import com.smart_extract_service.modules.bulk.domain.model.BulkJob;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BulkJobRepositoryPort {
    BulkJob save(BulkJob job);
    Optional<BulkJob> findById(UUID tenantId, UUID id);
    List<BulkJob> findByUser(UUID tenantId, UUID userId, int limit);
    List<BulkJob> findQueuedJobs(int maxJobs);
    boolean tryLock(UUID jobId);
    void releaseLock(UUID jobId);
    void updateStatus(UUID jobId, BulkJobStatus status);
    void updateProgress(UUID jobId, int processedItems, int successItems, int errorItems);
    void markFailed(UUID jobId, String errorMessage);
}
