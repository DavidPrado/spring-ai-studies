package com.smart_extract_service.core.config;

import com.smart_extract_service.core.context.TenantContext;
import com.smart_extract_service.core.context.UserContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncVirtualThreadConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor(){
        Executor delegate = Executors.newVirtualThreadPerTaskExecutor();

        return command -> {
            UUID tenant = TenantContext.getCurrentTenant();
            UUID user   = UserContext.getUserId();
            delegate.execute(() -> {
                TenantContext.setCurrentTenant(tenant);
                UserContext.setUserId(user);
                try {
                    command.run();
                } finally {
                    TenantContext.clear();
                    UserContext.clear();
                }
            });
        };
    }
}
