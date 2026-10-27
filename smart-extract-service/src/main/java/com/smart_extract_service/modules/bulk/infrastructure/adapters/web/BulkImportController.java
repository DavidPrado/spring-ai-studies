package com.smart_extract_service.modules.bulk.infrastructure.adapters.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.smart_extract_service.core.context.TenantContext;
import com.smart_extract_service.modules.bulk.application.spi.BulkEntityHandler;
import com.smart_extract_service.modules.bulk.domain.model.BulkJob;
import com.smart_extract_service.modules.bulk.domain.ports.input.BulkJobUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/v1/bulk")
@RequiredArgsConstructor
public class BulkImportController {

    private final BulkJobUseCase jobUseCase;
    private final ObjectMapper objectMapper;
    private final Map<String, BulkEntityHandler> handlers;

    @PostMapping("/{entity}/import")
    @PreAuthorize("hasAuthority('bulk:import')")
    public ResponseEntity<Map<String, Object>> createImportJob(@PathVariable String entity, @RequestBody Map<String, Object> request) {
        if (request.containsKey("data") || !(request.get("data") instanceof List)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Payload deve conter array 'data'"));
        }


        List<Map<String, String>> data = (List<Map<String, String>>) request.get("data");
        String mode = (String) request.getOrDefault("mode", "UPSERT");

        if (data.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Array 'data' não pode estar vazio"));
        }

        if (data.size() > 1000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Máximo de 1000 itens por requisição"));
        }
        UUID tenantId = TenantContext.getCurrentTenant();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = UUID.fromString(username);

        String payloadJson;

        try {
            payloadJson = objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erro ao serializar payload"));
        }

        BulkJob job = jobUseCase.createImportJob(tenantId, userId, username, entity, "IMPORT_JSON", mode, "JSON", payloadJson, data.size());

        log.info("📝 [IMPORT] Job criado: id={}, entity={}, totalItems={}", job.getId(), entity, data.size());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("jobId", job.getId().toString(), "status", job.getStatus().name(), "totalItems", job.getTotalItems(), "message", "Job criado. Acompanhe em /v1/bulk/jobs/" + job.getId()));

    }

    @GetMapping("/jobs")
    @PreAuthorize("hasAuthority('bulk:import')")
    public ResponseEntity<List<Map<String, Object>>> listUserJobs() {
        UUID tenantId = TenantContext.getCurrentTenant();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = UUID.fromString(username);

        List<BulkJob> jobs = jobUseCase.findUserJobs(tenantId, userId, 20);

        // Usamos HashMap para evitar o limite de 10 pares do Map.of()
        List<Map<String, Object>> response = jobs.stream().map(job -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", job.getId().toString());
            map.put("entity", job.getEntity());
            map.put("operation", job.getOperation());
            map.put("status", job.getStatus().name());
            map.put("totalItems", job.getTotalItems());
            map.put("processedItems", job.getProcessedItems());
            map.put("successItems", job.getSuccessItems());
            map.put("errorItems", job.getErrorItems());
            map.put("progressPercent", job.getProgressPercent());
            map.put("queuedAt", job.getQueuedAt().toString());
            map.put("startedAt", job.getStartedAt() != null ? job.getStartedAt().toString() : null);
            map.put("finishedAt", job.getFinishedAt() != null ? job.getFinishedAt().toString() : null);
            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/jobs/{jobId}")
    @PreAuthorize("hasAuthority('bulk:import')")
    public ResponseEntity<Map<String, Object>> getJobDetail(@PathVariable UUID jobId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        BulkJob job = jobUseCase.findJobById(tenantId, jobId);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", job.getId().toString());
        response.put("entity", job.getEntity());
        response.put("operation", job.getOperation());
        response.put("mode", job.getMode() != null ? job.getMode() : "");
        response.put("status", job.getStatus().name());
        response.put("totalItems", job.getTotalItems());
        response.put("processedItems", job.getProcessedItems());
        response.put("successItems", job.getSuccessItems());
        response.put("errorItems", job.getErrorItems());
        response.put("progressPercent", job.getProgressPercent());
        response.put("remainingSeconds", job.estimateRemainingSeconds());
        response.put("queuedAt", job.getQueuedAt().toString());
        response.put("startedAt", job.getStartedAt() != null ? job.getStartedAt().toString() : null);
        response.put("finishedAt", job.getFinishedAt() != null ? job.getFinishedAt().toString() : null);
        response.put("errorSummary", job.getErrorSummary() != null ? job.getErrorSummary() : "");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs/{jobId}/cancel")
    @PreAuthorize("hasAuthority('bulk:import')")
    public ResponseEntity<Map<String, Object>> cancelJob(@PathVariable UUID jobId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        boolean cancelled = jobUseCase.requestCancel(tenantId, jobId);

        if (cancelled) {
            return ResponseEntity.ok(Map.of("message", "Cancelamento solicitado"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Job não pode ser cancelado"));
        }
    }

    @GetMapping("/{entity}/template")
    @PreAuthorize("hasAuthority('bulk:import')")
    public ResponseEntity<List<Map<String, String>>> getTemplate(@PathVariable String entity) {
        BulkEntityHandler handler = handlers.get(entity);
        if (handler == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(handler.generateTemplate());
    }

    @PostMapping("/{entity}/import/csv")
    @PreAuthorize("hasAuthority('bulk:import')")
    public ResponseEntity<Map<String, Object>> importCsv(@PathVariable String entity, @RequestParam("file") MultipartFile file, @RequestParam(defaultValue = "UPSERT") String mode) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Arquivo vazio"));
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Apenas arquivos .csv são permitidos"));
        }

        try {

            List<Map<String, String>> data = parseCsv(file);

            if (data.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Arquivo CSV sem dados"));
            }

            if (data.size() > 1000) {
                return ResponseEntity.badRequest().body(Map.of("error", "Máximo de 1000 linhas por arquivo. Divida o arquivo em partes menores."));
            }

            UUID tenantId = TenantContext.getCurrentTenant();
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            UUID userId = UUID.fromString(username);

            String payloadJson = objectMapper.writeValueAsString(data);

            BulkJob job = jobUseCase.createImportJob(tenantId, userId, username, entity, "IMPORT_CSV", mode, "JSON", payloadJson, data.size());

            log.info("📝 [IMPORT CSV] Job criado: id={}, entity={}, totalItems={}", job.getId(), entity, data.size());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("jobId", job.getId().toString(), "status", job.getStatus().name(), "totalItems", job.getTotalItems(), "message", "Arquivo recebido. Processamento iniciado."));

        } catch (Exception e) {
            log.error("Erro ao processar upload CSV", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Erro interno ao processar arquivo: " + e.getMessage()));
        }
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws Exception {
        List<Map<String, String>> result = new ArrayList<>();

        char separator = detectDelimiter(file);

        try (InputStreamReader reader =
                     new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(new CSVParserBuilder()
                             .withSeparator(separator)
                             .withIgnoreQuotations(false)
                             .build())
                     .build()) {

            String[] headers = csvReader.readNext();
            if (headers == null || headers.length == 0) {
                return result;
            }

            if (headers[0].startsWith("\uFEFF")) {
                headers[0] = headers[0].substring(1).trim();
            }

            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                if (nextLine.length == 1 && nextLine[0].trim().isEmpty()) {
                    continue;
                }

                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String header = headers[i];
                    String value = (i < nextLine.length) ? nextLine[i].trim() : "";
                    row.put(header, value);
                }
                result.add(row);
            }
        }
        return result;

    }

    private char detectDelimiter(MultipartFile file) throws Exception {
        try (java.io.BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();
            if (firstLine == null) return ';';

            long semicolons = firstLine.chars().filter(ch -> ch == ';').count();
            long commas = firstLine.chars().filter(ch -> ch == ',').count();

            return (semicolons > commas) ? ';' : ',';
        }
    }


}
