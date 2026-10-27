package com.smart_extract_service.modules.bulk.application.dto;

import java.util.List;

public record RowError(
        int lineNumber,
        String rowData,
        List<String> errorMessages
) {
    public String formattedErrors() {
        return String.join(" | ", this.errorMessages);
    }
}
