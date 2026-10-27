package com.smart_extract_service.core.monitoring;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsageMonitoringService {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void captureDailyMetrics() {
        String sql = """
            INSERT INTO tenant_metrics_history (tenant_id, total_users, storage_size_mb)
            SELECT
                id,
                (SELECT count(*) FROM users u WHERE u.tenant_id = t.id),
                (SELECT count(*) * 0.002 FROM users u WHERE u.tenant_id = t.id)
            FROM tenants t
        """;
        jdbcTemplate.execute(sql);
    }

}
