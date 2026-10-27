package com.smart_extract_service.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class BatchProcessingConfig {

    @Bean(name = "batchProcessingExecutor")
    public Executor batchProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int cores = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(cores);
        executor.setMaxPoolSize(cores * 2);

        executor.setQueueCapacity(500);

        executor.setThreadNamePrefix("batch-");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        System.out.printf("""
            
            ╔══════════════════════════════════════╗
            ║   📦 BATCH PROCESSOR CONFIGURADO     ║
            ╠══════════════════════════════════════╣
            ║ Threads: %d                          ║
            ║ Fila: 500 tarefas                    ║
            ╚══════════════════════════════════════╝
            %n""", cores);

        return executor;
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(
            JdbcTemplate jdbcTemplate) {
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }
}
