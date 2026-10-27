package com.smart_extract_service.modules.bulk.domain.ports.input;

import com.smart_extract_service.modules.bulk.domain.model.BulkJob;

import java.util.List;
import java.util.UUID;

public interface BulkJobUseCase {

    BulkJob createImportJob(
            UUID tenantId, UUID userId, String username,
            String entity, String operation, String mode,
            String payloadType, String payloadJson, int totalItems);

    List<BulkJob> findUserJobs(UUID tenantId, UUID userId, int limit);

    BulkJob findJobById(UUID tenantId, UUID jobId);

    boolean requestCancel(UUID tenantId, UUID jobId);
}
