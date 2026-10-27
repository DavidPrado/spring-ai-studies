package com.smart_extract_service.modules.bulk.infrastructure.adapters.db.entities;

import com.smart_extract_service.modules.bulk.domain.enums.BulkJobStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bulk_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkJobEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "entity", nullable = false, length = 100)
    private String entity;

    @Column(name = "operation", nullable = false, length = 20)
    private String operation;

    @Column(name = "mode", length = 20)
    private String mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BulkJobStatus status;

    @Column(name = "total_items", nullable = false)
    private int totalItems;

    @Column(name = "processed_items", nullable = false)
    private int processedItems;

    @Column(name = "success_items", nullable = false)
    private int successItems;

    @Column(name = "error_items", nullable = false)
    private int errorItems;

    @Column(name = "payload_type", nullable = false, length = 10)
    private String payloadType;

    @Column(name = "payload_json", columnDefinition = "jsonb")
    private String payloadJson;

    @Column(name = "payload_file_path")
    private String payloadFilePath;

    @Column(name = "error_file_path")
    private String errorFilePath;

    @Column(name = "error_summary", columnDefinition = "jsonb")
    private String errorSummary;

    @Column(name = "worker_id", length = 100)
    private String workerId;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "queued_at", nullable = false)
    private LocalDateTime queuedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.queuedAt == null) {
            this.queuedAt = LocalDateTime.now();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
