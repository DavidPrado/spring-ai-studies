package com.smart_extract_service.modules.bulk.infrastructure.adapters.web;

import com.smart_extract_service.core.context.TenantContext;
import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.modules.bulk.domain.ports.input.BulkExportUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/bulk")
public class BulkExportController {

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Value("${app.bulk.max-sync-rows:1000}")
    private long maxSyncRows;

    @Value("${app.bulk.max-streaming-rows:10000}")
    private long maxStreamingRows;

    private final BulkExportUseCase useCase;

    public BulkExportController(BulkExportUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/{entity}/export")
    @PreAuthorize("hasAuthority('bulk:export')")
    public ResponseEntity<?> export(@PathVariable String entity, @RequestParam(defaultValue = "xlsx") String format, @RequestParam Map<String, String> filters) {

        if (!"csv".equalsIgnoreCase(format) && !"xlsx".equalsIgnoreCase(format)) {
            throw new BusinessException("bulk.format_invalid", "Formato '%s' inválido. Use 'csv' ou 'xlsx'.".formatted(format));
        }

        UUID tenantId = TenantContext.getCurrentTenant();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        long total = useCase.count(tenantId, entity, filters);

        if (total == 0) {
            return ResponseEntity.noContent().build();
        }
        if (total <= maxSyncRows) {
            return exportSincrono(entity, format, filters, tenantId, username, total);
        } else if (total <= maxStreamingRows) {
            return exportStreaming(entity, format, filters, tenantId, username, total);
        } else {
            return exportAsyncJob(entity, format, filters, tenantId, username, total);
        }
    }

    private ResponseEntity<byte[]> exportSincrono(String entity, String format, Map<String, String> filters, UUID tenantId, String username, long total) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            useCase.write(format, tenantId, username, entity, filters, total, out);
            byte[] fileBytes = out.toByteArray();
            return buildFileResponse(fileBytes, entity, format);

        } catch (IOException e) {

            throw new BusinessException("bulk.io_error", "Erro ao gerar arquivo: " + e.getMessage());
        } catch (Exception e) {

            throw new BusinessException("bulk.export_error", "Erro ao exportar dados: " + e.getMessage());
        }
    }

    private ResponseEntity<StreamingResponseBody> exportStreaming(
            String entity, String format, Map<String, String> filters,
            UUID tenantId, String username, long total) {
        StreamingResponseBody body = out -> {
            try {
                TenantContext.setCurrentTenant(tenantId);
                useCase.write(format, tenantId, username, entity, filters, total, out);
            }catch (Exception e) {
                throw new RuntimeException("Erro ao gerar arquivo: " + e.getMessage(), e);
            } finally {
                TenantContext.clear();
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + buildFilename(entity, format) + "\"")
                .header(HttpHeaders.CONTENT_TYPE, getContentType(format))
                .body(body);
    }

    private ResponseEntity<?> exportAsyncJob(
            String entity, String format, Map<String, String> filters,
            UUID tenantId, String username, long total) {

        return ResponseEntity.status(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "error", "bulk.export_too_large",
                        "message", "A exportação tem %d linhas (limite síncrono: %d, streaming: %d). "
                                .formatted(total, maxSyncRows, maxStreamingRows)
                                + "Refine os filtros ou aguarde a implementação de exportação assíncrona (FEAT-B04).",
                        "totalRows", total,
                        "maxSyncRows", maxSyncRows,
                        "maxStreamingRows", maxStreamingRows
                ));

    }

    private ResponseEntity<byte[]> buildFileResponse(
            byte[] fileBytes, String entity, String format) {

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + buildFilename(entity, format) + "\"")
                .header(HttpHeaders.CONTENT_TYPE, getContentType(format))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileBytes.length))
                .body(fileBytes);
    }

    private String buildFilename(String entity, String format) {
        String extension = "xlsx".equalsIgnoreCase(format) ? ".xlsx" : ".csv";
        return "%s_%s%s".formatted(entity, LocalDateTime.now().format(FILE_STAMP), extension);
    }

    private String getContentType(String format) {
        if ("xlsx".equalsIgnoreCase(format)) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        return "text/csv; charset=UTF-8";
    }

}