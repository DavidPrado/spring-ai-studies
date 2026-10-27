package com.smart_extract_service.modules.auth.infrastructure.adapters.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@ConditionalOnProperty(name = "login.history.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class LoginHistoryCleanupScheduler {

    private final com.smart_extract_service.modules.auth.application.usecases.LoginHistoryCleanupService cleanupService;

    public LoginHistoryCleanupScheduler(com.smart_extract_service.modules.auth.application.usecases.LoginHistoryCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @Scheduled(cron = "0 0 * * * *", zone = "America/Sao_Paulo")
    public void monitorHistoryGrowth() {
        try {
            var stats = cleanupService.getCleanupStatistics();

            long totalRecords = ((Number) stats.getOrDefault("total_records", 0L)).longValue();
            long oldRecords = ((Number) stats.getOrDefault("old_records_30days", 0L)).longValue();
            String tableSize = stats.getOrDefault("table_size", "0 bytes").toString();

            if (oldRecords > 100000) {
                log.warn("⚠️ ALERTA: {} registros antigos pendentes de limpeza!", oldRecords);
            }

            if (totalRecords > 0) {
                double percentOld = (oldRecords * 100.0) / totalRecords;
                log.debug("Estatísticas do histórico: Total={}, Antigos={} ({}%), Tamanho={}",
                        totalRecords, oldRecords, String.format("%.2f", percentOld), tableSize);
            } else {
                log.debug("Histórico de login vazio");
            }

        } catch (Exception e) {
            log.error("Erro no monitoramento do histórico de login: {}", e.getMessage());
        }
    }
}