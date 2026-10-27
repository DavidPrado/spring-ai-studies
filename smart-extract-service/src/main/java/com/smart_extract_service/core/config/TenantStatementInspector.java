package com.smart_extract_service.core.config;

import com.smart_extract_service.core.context.TenantContext;
import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.UUID;

public class TenantStatementInspector implements StatementInspector {

    @Override
    public String inspect(String sql) {
        if (sql.startsWith("/*") || sql.contains("/* tenant_id:")) {
            return sql;
        }

        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return "/* tenant_id: " + tenantId + " */ " + sql;
        }
        return sql;
    }
}

