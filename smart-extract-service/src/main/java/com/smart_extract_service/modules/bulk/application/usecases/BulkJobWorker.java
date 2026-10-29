package com.smart_extract_service.modules.bulk.application.usecases;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart_extract_service.modules.bulk.application.dto.ImportResult;
import com.smart_extract_service.modules.bulk.application.dto.RowError;
import com.smart_extract_service.modules.bulk.application.spi.BulkEntityHandler;
import com.smart_extract_service.modules.bulk.domain.enums.BulkJobStatus;
import com.smart_extract_service.modules.bulk.domain.model.BulkJob;
import com.smart_extract_service.modules.bulk.domain.ports.output.BulkJobRepositoryPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkJobWorker {

    private final BulkJobRepositoryPort repository;
    private final Map<String, BulkEntityHandler> handlers;
    private final ObjectMapper objectMapper;

    @Value("${app.bulk.worker.pool-size:3}")
    private int poolSize;

    @Value("${app.bulk.worker.batch-size:50}")
    private int batchSize;

    @Value("${app.bulk.worker.rate-limit-ms:100}")
    private int rateLimitMs;

    private final AtomicInteger threadCount = new AtomicInteger(0);

    private ExecutorService workPool;

    @PostConstruct
    public void initWorkerPool() {
        int safePoolSize = (this.poolSize <= 0) ? 3 : this.poolSize;

        log.info("🚀 Inicializando Pool de Workers com tamanho seguro: {}", safePoolSize);

        this.workPool = new ThreadPoolExecutor(
                safePoolSize,
                safePoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), // LinkedBlockingQueue é o padrão recomendado
                r -> {
                    Thread t = new Thread(r, "bulk-worker-" + threadCount.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Scheduled(fixedDelayString = "${app.bulk.worker.poll-interval-ms:500000}")
    public void pollAndProcess(){
        try{
            List<BulkJob> queuedJobs = repository.findQueuedJobs(poolSize);

            if(queuedJobs.isEmpty()){
                log.trace("📭 [WORKER] Nenhum job QUEUED encontrado");
                return;
            }

            log.info("📬 [WORKER] {} jobs QUEUED encontrados", queuedJobs.size());

            for(BulkJob job : queuedJobs){
                if(repository.tryLock(job.getId())){
                    workPool.submit(() -> processJob(job));
                }else{
                    log.debug(" [WORKER] Job {} já está sendo processado", job.getId());
                }
            }
        } catch (Exception e){
            log.error("❌ [WORKER] Erro no loop de polling", e);
        }
    }

    private void processJob(BulkJob job) {
        String workerId = Thread.currentThread().getName();
        log.info("🚀 [{}] Iniciando job: entity={}, totalItems={}",
                workerId, job.getEntity(), job.getTotalItems());

        try {
            job.startProcessing(workerId);
            repository.save(job);

            BulkEntityHandler handler = handlers.get(job.getEntity());
            if (handler == null) {
                throw new RuntimeException("Nenhum handler para entidade: " + job.getEntity());
            }

            List<Map<String, String>> rows = parsePayload(job);

            int totalProcessed = 0;
            int totalSuccess = 0;
            int totalErrors = 0;
            List<RowError> allErrors = new ArrayList<>();

            for (int i = 0; i < rows.size(); i += batchSize) {
                if (job.getStatus() == BulkJobStatus.CANCELLING) {
                    log.info("🛑 [{}] Cancelamento solicitado, parando job {}", workerId, job.getId());
                    job.confirmCancel();
                    repository.save(job);
                    break;
                }

                List<Map<String, String>> batch = rows.subList(i, Math.min(i + batchSize, rows.size()));

                ImportResult result = processBatch(handler, job, batch);

                totalProcessed += result.totalRows();
                totalSuccess += result.successRows();
                totalErrors += result.errorRows();
                allErrors.addAll(result.errors());

                job.recordProgress(result.successRows(), result.errorRows());
                repository.updateProgress(job.getId(), job.getProcessedItems(), job.getSuccessItems(), job.getErrorItems());

                log.debug("📊 [{}] Job {} progresso: {}/{} ({}%)",
                        workerId, job.getId(), job.getProcessedItems(), job.getTotalItems(), job.getProgressPercent());

                if (rateLimitMs > 0) {
                    Thread.sleep(rateLimitMs);
                }
            }

            if (job.getStatus() != BulkJobStatus.CANCELLING) {
                if (totalErrors == 0) {
                    job.markDone();
                    log.info("✅ [{}] Job {} concluído: {} itens", workerId, job.getId(), totalSuccess);
                } else {
                    job.markPartial(null, serializeErrors(allErrors));
                    log.warn("⚠️ [{}] Job {} com erros: {} sucessos, {} erros",
                            workerId, job.getId(), totalSuccess, totalErrors);
                }
                repository.save(job);
            }

        } catch (Exception e) {
            log.error(" [{}] Erro ao processar job {}", workerId, job.getId(), e);
            job.markFailed(e.getMessage());
            repository.save(job);
        } finally {
            repository.releaseLock(job.getId());
            log.info("🔓 [{}] Lock liberado para job {}", workerId, job.getId());
        }
    }


    private ImportResult processBatch(BulkEntityHandler handler, BulkJob job, List<Map<String, String>> batch) {
        List<RowError> errors = new ArrayList<>();
        int successCount = 0;

        List<Map<String, String>> validRows = new ArrayList<>();
        for (int i = 0; i < batch.size(); i++) {
            Map<String, String> row = batch.get(i);
            List<String> rowErrors = handler.validateRow(row);

            if (rowErrors.isEmpty()) {
                validRows.add(row);
            } else {
                String rowJson;
                try {
                    rowJson = objectMapper.writeValueAsString(row);
                } catch (Exception e) {
                    log.warn("Falha ao serializar linha {} como JSON", i + 1);
                    rowJson = "{}";
                }
                errors.add(new RowError(i + 1, rowJson, rowErrors));
            }
        }

        if (!validRows.isEmpty()) {
            try {
                ImportResult result = handler.processBatch(job.getTenantId(), validRows, job.getMode());
                successCount = result.successRows();
                errors.addAll(result.errors());
            } catch (Exception e) {
                log.error("❌ Erro ao processar lote do job {}", job.getId(), e);

                String mensagemAmigavel = "Erro ao salvar no banco de dados. Verifique os dados e tente novamente.";

                for (int i = 0; i < validRows.size(); i++) {
                    String rowJson;
                    try {
                        rowJson = objectMapper.writeValueAsString(validRows.get(i));
                    } catch (Exception ex) {
                        rowJson = "{}";
                    }
                    errors.add(new RowError(i + 1, rowJson, List.of(mensagemAmigavel)));
                }
            }
        }

        return new ImportResult(batch.size(), successCount, errors.size(), errors);
    }




    private List<Map<String, String>> parsePayload(BulkJob job) {
        if (!"JSON".equals(job.getPayloadType())) {
            throw new RuntimeException("Payload type não suportado: " + job.getPayloadType());
        }
        if (job.getPayloadJson() == null) {
            throw new RuntimeException("Payload JSON está vazio");
        }

        try {
            return objectMapper.readValue(job.getPayloadJson(), new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Erro ao parsear payload JSON", e);
        }
    }

    private String serializeErrors(List<RowError> errors) {
        try {
            List<RowError> limited = errors.stream().limit(100).collect(Collectors.toList());
            return objectMapper.writeValueAsString(limited);
        } catch (Exception e) {
            log.error("Erro ao serializar erros", e);
            return "[]";
        }
    }

}
