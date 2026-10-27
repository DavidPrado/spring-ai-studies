package com.smart_extract_service.modules.bulk.domain.model;

import com.smart_extract_service.modules.bulk.domain.enums.BulkJobStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public class BulkJob {

    private final UUID id;
    private final UUID tenantId;
    private final UUID userId;
    private final String username;
    private final String entity;
    private final String operation;
    private final String mode;
    private final String payloadType;
    private final String payloadJson;
    private final String payloadFilePath;

    private BulkJobStatus status;
    private int totalItems;
    private int processedItems;
    private int successItems;
    private int errorItems;
    private String errorFilePath;
    private String errorSummary;
    private String workerId;
    private int retryCount;
    private String lastError;

    private final LocalDateTime queuedAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    private BulkJob(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.tenantId = builder.tenantId;
        this.userId = builder.userId;
        this.username = builder.username;
        this.entity = builder.entity;
        this.operation = builder.operation;
        this.mode = builder.mode;
        this.payloadType = builder.payloadType;
        this.payloadJson = builder.payloadJson;
        this.payloadFilePath = builder.payloadFilePath;


        this.status = BulkJobStatus.QUEUED;
        this.queuedAt = LocalDateTime.now();


        this.totalItems = 0;
        this.processedItems = 0;
        this.successItems = 0;
        this.errorItems = 0;
        this.retryCount = 0;
    }

    public void startProcessing(String workerId){
        if(this.status != BulkJobStatus.QUEUED){
            throw new IllegalStateException("Job %s não pode iniciar: status atual é %s (esperado QUEUED)" .formatted(this.id, this.status));
        }
        this.status = BulkJobStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
        this.workerId = workerId;
    }

    public void recordProgress(int batchSuccess, int batchErrors){
        if(this.status != BulkJobStatus.PROCESSING && this.status != BulkJobStatus.CANCELLING){
            throw new IllegalStateException(
                    "Job %s não está em processamento (status: %s)"
                            .formatted(this.id, this.status));
        }

        this.processedItems += (batchSuccess + batchErrors);
        this.successItems += batchSuccess;
        this.errorItems += batchErrors;

        if(this.processedItems > this.totalItems){
            throw new IllegalStateException(
                    "Job %s: processedItems (%d) excedeu totalItems (%d)"
                            .formatted(this.id, this.processedItems, this.totalItems));
        }
    }

    public void markDone(){
        validateProcessingState();
        this.status = BulkJobStatus.DONE;
        this.finishedAt = LocalDateTime.now();
    }


    public void markPartial(String errorFilePath, String errorSummary){
        validateProcessingState();
        this.status = BulkJobStatus.PARTIAL;
        this.errorFilePath = errorFilePath;
        this.errorSummary = errorSummary;
        this.finishedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage){
        this.status = BulkJobStatus.FAILED;
        this.lastError = errorMessage;
        this.finishedAt = LocalDateTime.now();
        this.retryCount++;
    }

    public boolean requestCancel(){
        if(!this.status.isCancellable()){
            return false;
        }

        if(this.status == BulkJobStatus.PROCESSING){
            this.status = BulkJobStatus.CANCELLING;
        }else{
            this.status = BulkJobStatus.CANCELLED;
            this.finishedAt = LocalDateTime.now();
        }
        return true;
    }

    public void confirmCancel() {
        if (this.status != BulkJobStatus.CANCELLING) {
            throw new IllegalStateException(
                    "Job %s não está em CANCELLING (status: %s)"
                            .formatted(this.id, this.status));
        }
        this.status = BulkJobStatus.CANCELLED;
        this.finishedAt = LocalDateTime.now();
    }

    private void validateProcessingState() {
        if (this.status != BulkJobStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Job %s não está em PROCESSING (status: %s)"
                            .formatted(this.id, this.status));
        }
    }

    public int getProgressPercent() {
        if (this.totalItems == 0) return 0;
        return (int) ((this.processedItems * 100L) / this.totalItems);
    }

    public Long estimateRemainingSeconds() {
        if (this.startedAt == null || this.processedItems == 0) {
            return null;
        }
        long elapsedSeconds = java.time.temporal.ChronoUnit.SECONDS.between(
                this.startedAt,
                java.time.LocalDateTime.now()
        );
        if (elapsedSeconds <= 0) {
            return null;
        }
        double rate = (double) this.processedItems / elapsedSeconds;
        long remaining = this.totalItems - this.processedItems;

        return (long) (remaining / rate);
    }

    public boolean isTerminal() {
        return this.status.isTerminal();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEntity() {
        return entity;
    }

    public String getOperation() {
        return operation;
    }

    public String getMode() {
        return mode;
    }

    public String getPayloadType() {
        return payloadType;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public String getPayloadFilePath() {
        return payloadFilePath;
    }

    public BulkJobStatus getStatus() {
        return status;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getProcessedItems() {
        return processedItems;
    }

    public int getSuccessItems() {
        return successItems;
    }

    public int getErrorItems() {
        return errorItems;
    }

    public String getErrorFilePath() {
        return errorFilePath;
    }

    public String getErrorSummary() {
        return errorSummary;
    }

    public String getWorkerId() {
        return workerId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getLastError() {
        return lastError;
    }

    public LocalDateTime getQueuedAt() {
        return queuedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setStatus(BulkJobStatus status) {
        this.status = status;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public void setProcessedItems(int processedItems) {
        this.processedItems = processedItems;
    }

    public void setSuccessItems(int successItems) {
        this.successItems = successItems;
    }

    public void setErrorItems(int errorItems) {
        this.errorItems = errorItems;
    }

    public void setErrorFilePath(String errorFilePath) {
        this.errorFilePath = errorFilePath;
    }

    public void setErrorSummary(String errorSummary) {
        this.errorSummary = errorSummary;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID id;
        private UUID tenantId;
        private UUID userId;
        private String username;
        private String entity;
        private String operation;
        private String mode;
        private String payloadType;
        private String payloadJson;
        private String payloadFilePath;

        // Métodos fluentes (retornam this para encadeamento)
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder tenantId(UUID tenantId) { this.tenantId = tenantId; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder entity(String entity) { this.entity = entity; return this; }
        public Builder operation(String operation) { this.operation = operation; return this; }
        public Builder mode(String mode) { this.mode = mode; return this; }
        public Builder payloadType(String payloadType) { this.payloadType = payloadType; return this; }
        public Builder payloadJson(String payloadJson) { this.payloadJson = payloadJson; return this; }
        public Builder payloadFilePath(String payloadFilePath) { this.payloadFilePath = payloadFilePath; return this; }

        /**
         * Constrói o BulkJob com validações.
         *
         * VALIDAÇÕES:
         *   → tenantId, userId, username, entity, operation são obrigatórios
         *   → payloadType deve ser "JSON" ou "FILE"
         *   → Se payloadType=JSON, payloadJson não pode ser null
         *   → Se payloadType=FILE, payloadFilePath não pode ser null
         */
        public BulkJob build() {
            if (this.tenantId == null) {
                throw new IllegalArgumentException("tenantId é obrigatório");
            }
            if (this.userId == null) {
                throw new IllegalArgumentException("userId é obrigatório");
            }
            if (this.username == null || this.username.isBlank()) {
                throw new IllegalArgumentException("username é obrigatório");
            }
            if (this.entity == null || this.entity.isBlank()) {
                throw new IllegalArgumentException("entity é obrigatório");
            }
            if (this.operation == null || this.operation.isBlank()) {
                throw new IllegalArgumentException("operation é obrigatório");
            }
            if (this.payloadType == null) {
                throw new IllegalArgumentException("payloadType é obrigatório");
            }
            if (!this.payloadType.equals("JSON") && !this.payloadType.equals("FILE")) {
                throw new IllegalArgumentException("payloadType deve ser 'JSON' ou 'FILE'");
            }
            if (this.payloadType.equals("JSON") && this.payloadJson == null) {
                throw new IllegalArgumentException("payloadJson é obrigatório para payloadType=JSON");
            }
            if (this.payloadType.equals("FILE") && this.payloadFilePath == null) {
                throw new IllegalArgumentException("payloadFilePath é obrigatório para payloadType=FILE");
            }

            return new BulkJob(this);
        }
    }
}
