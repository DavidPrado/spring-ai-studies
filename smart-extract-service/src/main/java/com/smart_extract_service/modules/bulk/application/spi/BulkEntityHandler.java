package com.smart_extract_service.modules.bulk.application.spi;

import com.smart_extract_service.modules.bulk.application.dto.ImportResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface BulkEntityHandler {

    String entity();

    List<String> headers();

    long count(UUID tenantId, Map<String, String> filters);

    void streamRows(UUID tenantId, Map<String, String> filters,
                    Consumer<List<BulkCell>> rowSink);

    List<String> validateRow(Map<String, String> row);

    ImportResult processBatch(UUID tenantId, List<Map<String, String>> rows, String mode);

    List<Map<String, String>> generateTemplate();

}
