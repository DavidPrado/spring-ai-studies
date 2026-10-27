package com.smart_extract_service.modules.bulk.application.usecases;

import com.smart_extract_service.modules.bulk.domain.model.BulkJob;
import com.smart_extract_service.modules.bulk.domain.ports.input.BulkJobUseCase;
import com.smart_extract_service.modules.bulk.domain.ports.output.BulkJobRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkJobService implements BulkJobUseCase {

    private final BulkJobRepositoryPort repository;

    @Override
    @Transactional
    public BulkJob createImportJob(UUID tenantId,
                                   UUID userId, String username,
                                   String entity, String operation,
                                   String mode, String payloadType,
                                   String payloadJson, int totalItems) {

        if(totalItems <= 0){
            throw new IllegalArgumentException("totalItems deve ser > 0");
        }
        if(payloadJson == null && payloadType.equals("JSON")){
            throw new IllegalArgumentException("payloadJson é obrigatório para payloadType=JSON");
        }

        BulkJob job = BulkJob.builder()
                .tenantId(tenantId)
                .userId(userId)
                .username(username)
                .entity(entity)
                .operation(operation)
                .mode(mode)
                .payloadType(payloadType)
                .payloadJson(payloadJson)
                .build();

        job.setTotalItems(totalItems);

        return repository.save(job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BulkJob> findUserJobs(UUID tenantId, UUID userId, int limit) {
        return repository.findByUser(tenantId, userId, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public BulkJob findJobById(UUID tenantId, UUID jobId) {
        return repository.findById(tenantId, jobId).orElseThrow(() -> new RuntimeException("Job não encontrado " + jobId ));
    }

    @Override
    public boolean requestCancel(UUID tenantId, UUID jobId) {


        BulkJob job = repository.findById(tenantId, jobId)
                .orElseThrow(() -> new RuntimeException("Job não encontrado: " + jobId));

        boolean cancelled = job.requestCancel();

        if (cancelled) {
            repository.save(job);
            log.info("✅ [JOB] Cancelamento aceito: jobId={}, status={}", jobId, job.getStatus());
        } else{
            log.warn("⚠️ [JOB] Cancelamento recusado: jobId={}, status={}", jobId, job.getStatus());
        }
        return cancelled;
    }
}
