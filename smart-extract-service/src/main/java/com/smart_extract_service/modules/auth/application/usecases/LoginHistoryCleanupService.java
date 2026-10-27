package com.smart_extract_service.modules.auth.application.usecases;

import com.smart_extract_service.modules.auth.infrastructure.adapters.db.UserLoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHistoryCleanupService {

    private final UserLoginHistoryRepository historyRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${login.history.retention.days:30}")
    private int retentionDays;

    @Value("${login.history.cleanup.batch-size:5000}")
    private int batchSize;

    @Value("${login.history.cleanup.max-batches:100}")
    private int maxBatches;

    /**
     * Método principal de limpeza - Retorna CleanupResult
     */
    @Transactional
    public CleanupResult cleanupOldRecords() {
        long startTime = System.currentTimeMillis();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        log.info("Iniciando limpeza programada do histórico de login");
        log.info("Data de corte: {}", cutoffDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        CleanupResult result = new CleanupResult();
        result.setCutoffDate(cutoffDate);
        result.setRetentionDays(retentionDays);

        try {
            // Contar registros a serem deletados
            String countSql = "SELECT COUNT(*) FROM user_login_history WHERE login_at < ?";
            Long totalToDelete = jdbcTemplate.queryForObject(countSql, Long.class, cutoffDate);
            result.setTotalToDelete(totalToDelete != null ? totalToDelete : 0L);

            log.info("Registros a serem deletados: {}", result.getTotalToDelete());

            if (result.getTotalToDelete() == 0) {
                result.setSuccess(true);
                result.setTotalDeleted(0);
                result.setDurationMs(System.currentTimeMillis() - startTime);
                return result;
            }

            // Executar deleção em batches
            int totalDeleted = 0;
            int batchCount = 0;

            while (batchCount < maxBatches) {
                String deleteSql = "DELETE FROM user_login_history WHERE login_at < ? LIMIT ?";
                int deleted = jdbcTemplate.update(deleteSql, cutoffDate, batchSize);

                if (deleted == 0) {
                    break;
                }

                totalDeleted += deleted;
                batchCount++;

                log.info("Batch {}: deletados {} registros (acumulado: {}/{})",
                        batchCount, deleted, totalDeleted, result.getTotalToDelete());

                // Pequena pausa entre batches
                if (deleted == batchSize && batchCount < maxBatches) {
                    Thread.sleep(100);
                }
            }

            result.setTotalDeleted(totalDeleted);
            result.setBatchCount(batchCount);
            result.setSuccess(true);
            result.setDurationMs(System.currentTimeMillis() - startTime);

            log.info("✅ Limpeza concluída! {} registros deletados em {} batches ({} ms)",
                    totalDeleted, batchCount, result.getDurationMs());

            // Executar VACUUM se necessário
            if (totalDeleted > 10000) {
                jdbcTemplate.execute("VACUUM ANALYZE user_login_history");
            }

        } catch (Exception e) {
            log.error("❌ Erro durante limpeza", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setDurationMs(System.currentTimeMillis() - startTime);
        }

        return result;
    }

    /**
     * Limpeza específica por tenant
     */
    @Transactional
    public CleanupResult cleanupByTenant(java.util.UUID tenantId) {
        long startTime = System.currentTimeMillis();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        log.info("Iniciando limpeza para tenant: {}", tenantId);

        CleanupResult result = new CleanupResult();
        result.setCutoffDate(cutoffDate);
        result.setRetentionDays(retentionDays);

        try {
            String deleteSql = """
                DELETE FROM user_login_history 
                WHERE user_id IN (
                    SELECT id FROM users WHERE tenant_id = ?
                ) 
                AND login_at < ?
            """;

            int totalDeleted = jdbcTemplate.update(deleteSql, tenantId, cutoffDate);

            result.setTotalDeleted(totalDeleted);
            result.setBatchCount(1);
            result.setSuccess(true);
            result.setDurationMs(System.currentTimeMillis() - startTime);

            log.info("Limpeza do tenant {} concluída: {} registros deletados", tenantId, totalDeleted);

        } catch (Exception e) {
            log.error("Erro na limpeza do tenant", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setDurationMs(System.currentTimeMillis() - startTime);
        }

        return result;
    }

    /**
     * Obtém estatísticas da tabela
     */
    public Map<String, Object> getCleanupStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            String totalSql = "SELECT COUNT(*) FROM user_login_history";
            Long total = jdbcTemplate.queryForObject(totalSql, Long.class);
            stats.put("total_records", total != null ? total : 0L);

            String oldSql = "SELECT COUNT(*) FROM user_login_history WHERE login_at < NOW() - INTERVAL '30 days'";
            Long old = jdbcTemplate.queryForObject(oldSql, Long.class);
            stats.put("old_records_30days", old != null ? old : 0L);

            String oldestSql = "SELECT MIN(login_at) FROM user_login_history";
            LocalDateTime oldest = jdbcTemplate.queryForObject(oldestSql, LocalDateTime.class);
            stats.put("oldest_record", oldest);

            String newestSql = "SELECT MAX(login_at) FROM user_login_history";
            LocalDateTime newest = jdbcTemplate.queryForObject(newestSql, LocalDateTime.class);
            stats.put("newest_record", newest);

            String sizeSql = "SELECT pg_size_pretty(pg_total_relation_size('user_login_history'))";
            String size = jdbcTemplate.queryForObject(sizeSql, String.class);
            stats.put("table_size", size != null ? size : "0 bytes");

            stats.put("retention_days", retentionDays);
            stats.put("batch_size", batchSize);

        } catch (Exception e) {
            log.error("Erro ao obter estatísticas", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * DTO para resultado da limpeza - Classe interna pública
     */
    public static class CleanupResult {
        private boolean success;
        private long totalToDelete;
        private long totalDeleted;
        private int batchCount;
        private int retentionDays;
        private LocalDateTime cutoffDate;
        private long durationMs;
        private String errorMessage;

        // Getters e Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public long getTotalToDelete() { return totalToDelete; }
        public void setTotalToDelete(long totalToDelete) { this.totalToDelete = totalToDelete; }

        public long getTotalDeleted() { return totalDeleted; }
        public void setTotalDeleted(long totalDeleted) { this.totalDeleted = totalDeleted; }

        public int getBatchCount() { return batchCount; }
        public void setBatchCount(int batchCount) { this.batchCount = batchCount; }

        public int getRetentionDays() { return retentionDays; }
        public void setRetentionDays(int retentionDays) { this.retentionDays = retentionDays; }

        public LocalDateTime getCutoffDate() { return cutoffDate; }
        public void setCutoffDate(LocalDateTime cutoffDate) { this.cutoffDate = cutoffDate; }

        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}