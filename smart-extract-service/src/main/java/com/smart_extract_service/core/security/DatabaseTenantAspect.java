package com.smart_extract_service.core.security;

import com.smart_extract_service.core.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class DatabaseTenantAspect {

    private final JdbcTemplate jdbcTemplate;

    @Before("within(@org.springframework.stereotype.Service *) || within(@org.springframework.stereotype.Repository *)")
    public void setTenantInPostgres() {
        UUID tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null) {
            return;
        }

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException(String.format(
                    "Operação tenant-scoped sem transação ativa (tenant=%s). " +
                            "Adicione @Transactional ao método de serviço/repositório.",
                    tenantId));
        }

        jdbcTemplate.execute(
                String.format("SET LOCAL app.current_tenant = '%s'", tenantId));
    }

}