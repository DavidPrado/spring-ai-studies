package com.smart_extract_service.modules.bulk.infrastructure.adapters.db;


import com.smart_extract_service.modules.bulk.domain.enums.BulkJobStatus;
import com.smart_extract_service.modules.bulk.domain.model.BulkJob;
import com.smart_extract_service.modules.bulk.domain.ports.output.BulkJobRepositoryPort;
import com.smart_extract_service.modules.bulk.infrastructure.adapters.db.entities.BulkJobEntity;
import com.smart_extract_service.modules.bulk.infrastructure.adapters.db.mappers.BulkJobMapper;
import com.smart_extract_service.modules.bulk.infrastructure.adapters.db.repositories.BulkJobJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkJobPersistenceAdapter implements BulkJobRepositoryPort {

    private final BulkJobJpaRepository repository;
    private final DataSource dataSource;

    @Override
    @Transactional
    public BulkJob save(BulkJob job){
        BulkJobEntity entity = BulkJobMapper.toEntity(job);
        entity = repository.save(entity);
        return BulkJobMapper.toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BulkJob> findById(UUID tenantId, UUID id){
        return repository.findById(id)
                .filter(e -> e.getTenantId().equals(tenantId))
                .map(BulkJobMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BulkJob> findByUser(UUID tenantId, UUID userId, int limit){
        return repository.findTop20ByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId,userId)
                .stream()
                .limit(limit)
                .map(BulkJobMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BulkJob> findQueuedJobs(int maxJobs){
        return repository.findTopNByStatusOrderByQueuedAtAsc(
                BulkJobStatus.QUEUED,
                PageRequest.of(0,maxJobs)
        ).stream()
                .map(BulkJobMapper::toDomain)
                .toList();
    }

    @Override
    public boolean tryLock(UUID jobId) {
        String sql = "SELECT pg_try_advisory_lock(hashtext(?::text))";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jobId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean locked = rs.getBoolean(1);
                    if (locked) {
                        log.debug(" Advisory lock adquirido para job {}", jobId);
                    } else {
                        log.debug("🔓 Advisory lock NÃO adquirido para job {}", jobId);
                    }
                    return locked;
                }
            }
        } catch (Exception e) {
            log.error("Erro ao adquirir advisory lock para job {}", jobId, e);
        }
        return false;
    }

    @Override
    public void releaseLock(UUID jobId) {
        String sql = "SELECT pg_advisory_unlock(hashtext(?::text))";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jobId.toString());
            ps.executeQuery();

            log.debug(" Advisory lock liberado para job {}", jobId);
        } catch (Exception e) {
            log.error("Erro ao liberar advisory lock para job {}", jobId, e);
        }
    }

    @Override
    @Transactional
    public void updateStatus(UUID jobId, BulkJobStatus status) {
        repository.updateStatus(jobId, status);
    }

    @Override
    @Transactional
    public void updateProgress(UUID jobId, int processedItems, int successItems, int errorItems) {
        repository.updateProgress(jobId, processedItems, successItems, errorItems);
    }

    @Override
    @Transactional
    public void markFailed(UUID jobId, String errorMessage) {
        repository.markFailed(jobId, errorMessage);
    }
}
