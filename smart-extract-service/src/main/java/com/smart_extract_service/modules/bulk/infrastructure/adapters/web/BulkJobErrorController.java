package com.smart_extract_service.modules.bulk.infrastructure.adapters.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart_extract_service.core.context.TenantContext;
import com.smart_extract_service.modules.bulk.application.dto.RowError;
import com.smart_extract_service.modules.bulk.application.dto.RowErrorDetail;
import com.smart_extract_service.modules.bulk.domain.model.BulkJob;
import com.smart_extract_service.modules.bulk.domain.ports.input.BulkJobUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/v1/bulk/jobs")
@RequiredArgsConstructor
public class BulkJobErrorController {

    private final BulkJobUseCase jobUseCase;
    private final ObjectMapper objectMapper;

    // Regex para parsear formato {chave=valor, chave2=valor2}
    private static final Pattern MAP_ENTRY_PATTERN = Pattern.compile("(\\w+)=([^,}]+)");

    @GetMapping("/{jobId}/errors/detail")
    @PreAuthorize("hasAuthority('bulk:import')")
    public ResponseEntity<List<RowErrorDetail>> getJobErrorDetails(@PathVariable UUID jobId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        BulkJob job = jobUseCase.findJobById(tenantId, jobId);

        if (job.getErrorSummary() == null || job.getErrorSummary().isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        try {
            List<RowError> errors = objectMapper.readValue(
                    job.getErrorSummary(),
                    new TypeReference<List<RowError>>() {}
            );

            List<RowErrorDetail> details = errors.stream()
                    .map(err -> parseRowError(err))
                    .toList();

            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Erro ao parsear erros do job {}", jobId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private RowErrorDetail parseRowError(RowError err) {
        Map<String, String> rowData = new HashMap<>();

        try {
            // Tenta parsear como JSON primeiro
            rowData = objectMapper.readValue(
                    err.rowData(),
                    new TypeReference<Map<String, String>>() {}
            );
            return new RowErrorDetail(
                    err.lineNumber(),
                    rowData,
                    err.rowData(),
                    err.errorMessages(),
                    "VALIDATION_ERROR"
            );
        } catch (Exception e) {
            // Fallback: tenta parsear formato {chave=valor}
            log.debug("Falha ao parsear como JSON, tentando formato Map.toString()");
            rowData = parseMapToString(err.rowData());
            return new RowErrorDetail(
                    err.lineNumber(),
                    rowData,
                    err.rowData(),
                    err.errorMessages(),
                    "PARSE_ERROR"
            );
        }
    }

    /**
     * Parseia string no formato {chave=valor, chave2=valor2} para Map
     */
    private Map<String, String> parseMapToString(String mapString) {
        Map<String, String> result = new HashMap<>();
        if (mapString == null || mapString.isBlank()) {
            return result;
        }

        // Remove chaves { }
        String cleaned = mapString.trim();
        if (cleaned.startsWith("{")) cleaned = cleaned.substring(1);
        if (cleaned.endsWith("}")) cleaned = cleaned.substring(0, cleaned.length() - 1);

        Matcher matcher = MAP_ENTRY_PATTERN.matcher(cleaned);
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            result.put(key, value);
        }

        return result;
    }
}