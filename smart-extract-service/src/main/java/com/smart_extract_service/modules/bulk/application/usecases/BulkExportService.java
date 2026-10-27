package com.smart_extract_service.modules.bulk.application.usecases;

import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.modules.bulk.application.spi.BulkEntityHandler;
import com.smart_extract_service.modules.bulk.application.support.CsvStreamRenderer;
import com.smart_extract_service.modules.bulk.application.support.ExcelStreamRenderer;
import com.smart_extract_service.modules.bulk.domain.ports.input.BulkExportUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BulkExportService implements BulkExportUseCase {

    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<String, BulkEntityHandler> handlers;

    public BulkExportService(List<BulkEntityHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toUnmodifiableMap(
                        BulkEntityHandler::entity, Function.identity()));
    }

    @Override
    @Transactional(readOnly = true)
    public long count(UUID tenantId, String entity, Map<String, String> filters) {
        return handler(entity).count(tenantId, filters);
    }

    @Override
    @Transactional(readOnly = true)
    public void write(String format, UUID tenantId, String username,
                      String entity, Map<String, String> filters,
                      long totalRows, OutputStream out) {
        BulkEntityHandler handler = handler(entity);

        if ("xlsx".equalsIgnoreCase(format)) {
            try (ExcelStreamRenderer renderer = new ExcelStreamRenderer(out)) {
                writeMetadata(renderer, handler, username, filters, totalRows);
                renderer.headers(handler.headers());
                handler.streamRows(tenantId, filters, renderer::row);
                renderer.flush();
            }
        } else {
            // CSV é o padrão (fallback).
            try (CsvStreamRenderer renderer = new CsvStreamRenderer(out)) {
                writeMetadataCsv(renderer, handler, username, filters, totalRows);
                renderer.headers(handler.headers());
                handler.streamRows(tenantId, filters, cells ->
                        renderer.row(cells));
                renderer.flush();
            }
        }
    }

    private void writeMetadata(ExcelStreamRenderer renderer,
                               BulkEntityHandler handler, String username,
                               Map<String, String> filters, long totalRows) {
        renderer.comment("Moviment Tech — Exportacao de dados");
        renderer.comment("Entidade: " + handler.entity());
        renderer.comment("Usuario: " + username);
        renderer.comment("Gerado em: " + LocalDateTime.now().format(STAMP));
        renderer.comment("Filtros: " + (filters.isEmpty() ? "nenhum" : filters.toString()));
        renderer.comment("Total de linhas: " + totalRows);
    }

    private void writeMetadataCsv(CsvStreamRenderer renderer,
                                  BulkEntityHandler handler, String username,
                                  Map<String, String> filters, long totalRows) {
        renderer.comment("Moviment Tech — Exportacao de dados");
        renderer.comment("Entidade: " + handler.entity());
        renderer.comment("Usuario: " + username);
        renderer.comment("Gerado em: " + LocalDateTime.now().format(STAMP));
        renderer.comment("Filtros: " + (filters.isEmpty() ? "nenhum" : filters.toString()));
        renderer.comment("Total de linhas: " + totalRows);
    }

    @Override
    public List<String> supportedEntities() {
        return List.copyOf(handlers.keySet());
    }

    private BulkEntityHandler handler(String entity) {
        BulkEntityHandler h = handlers.get(entity);
        if (h == null) {
            throw new BusinessException("bulk.entity_not_supported",
                    "Entidade '%s' não é suportada para exportação. Suportadas: %s."
                            .formatted(entity, String.join(", ", handlers.keySet())));
        }
        return h;
    }
}