package com.smart_extract_service.modules.bulk.infrastructure.adapters.db.mappers;

import com.smart_extract_service.modules.bulk.domain.model.BulkJob;
import com.smart_extract_service.modules.bulk.infrastructure.adapters.db.entities.BulkJobEntity;

public final class BulkJobMapper {

    private BulkJobMapper() {}

    public static BulkJob toDomain(BulkJobEntity entity) {
        if (entity == null) {
            return null;
        }

        BulkJob job = BulkJob.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .entity(entity.getEntity())
                .operation(entity.getOperation())
                .mode(entity.getMode())
                .payloadType(entity.getPayloadType())
                .payloadJson(entity.getPayloadJson())
                .payloadFilePath(entity.getPayloadFilePath())
                .build();

        job.setTotalItems(entity.getTotalItems());
        job.setLastError(entity.getLastError());
        job.setRetryCount(entity.getRetryCount());

        return job;
    }

    public static BulkJobEntity toEntity(BulkJob job) {
        if (job == null) {
            return null;
        }

        return BulkJobEntity.builder()
                .id(job.getId())
                .tenantId(job.getTenantId())
                .userId(job.getUserId())
                .username(job.getUsername())
                .entity(job.getEntity())
                .operation(job.getOperation())
                .mode(job.getMode())
                .status(job.getStatus())
                .totalItems(job.getTotalItems())
                .processedItems(job.getProcessedItems())
                .successItems(job.getSuccessItems())
                .errorItems(job.getErrorItems())
                .payloadType(job.getPayloadType())
                .payloadJson(job.getPayloadJson())
                .payloadFilePath(job.getPayloadFilePath())
                .errorFilePath(job.getErrorFilePath())
                .errorSummary(job.getErrorSummary())
                .workerId(job.getWorkerId())
                .retryCount(job.getRetryCount())
                .lastError(job.getLastError())
                .queuedAt(job.getQueuedAt())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .build();
    }
}
