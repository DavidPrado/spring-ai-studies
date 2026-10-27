package com.smart_extract_service.core.context;

import com.smart_extract_service.modules.auth.application.dto.AuditLog;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(com.smart_extract_service.core.context.Auditable)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        UUID userId = UserContext.getUserId();
        UUID tenantId = TenantContext.getCurrentTenant();
        String methodName = joinPoint.getSignature().getName();
        String entityName = joinPoint.getTarget().getClass().getSimpleName();

        String entityId = extractEntityId(joinPoint.getArgs());

        Object result = joinPoint.proceed();

        AuditLog log = new AuditLog(
                userId,
                tenantId,
                methodName,
                entityName,
                entityId,
                null,
                Arrays.toString(joinPoint.getArgs()),
                "0.0.0.0",
                LocalDateTime.now()
        );

        String key = "audit:" + tenantId + ":" + UUID.randomUUID();
        redisTemplate.opsForValue().set(key, log, Duration.ofDays(30));

        return result;
    }

    private String extractEntityId(Object[] args) {
        if (args == null || args.length == 0) return "N/A";
        return args[0].toString();
    }
}
