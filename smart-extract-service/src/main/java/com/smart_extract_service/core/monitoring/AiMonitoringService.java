package com.smart_extract_service.core.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiMonitoringService {

    private final JdbcTemplate jdbcTemplate;
    private final long MAX_CHUNKS_PER_TENANT = 50000;

    @Scheduled(fixedRate = 3600000)
    public void monitorTenantUsage() {
        String sql = "SELECT tenant_id, count(*) as total FROM vector_store GROUP BY tenant_id";

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        for (Map<String, Object> row : results) {
            UUID tenantId = (UUID) row.get("tenant_id");
            long total = (long) row.get("total");

            if (total > MAX_CHUNKS_PER_TENANT) {
                triggerAlert(tenantId, total);
            }
        }
    }

    private void triggerAlert(UUID tenantId, long usage) {
        log.error("ALERT: Tenant {} exceeded AI storage limit! Current: {}", tenantId, usage);
    }
}
