package com.smart_extract_service.modules.bulk.domain.ports.input;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BulkExportUseCase {

    long count(UUID tenantId, String entity, Map<String, String> filters);

    void write(String format,UUID tenantId, String username, String entity, Map<String, String> filters, long totalRows, OutputStream out);

    List<String> supportedEntities();
}
