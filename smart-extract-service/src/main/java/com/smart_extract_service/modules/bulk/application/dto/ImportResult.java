package com.smart_extract_service.modules.bulk.application.dto;

import java.util.List;

public record ImportResult(

        int totalRows,

        int successRows,

        int errorRows,

        List<RowError> errors
) {

    public static ImportResult allSuccess(int totalRows) {
        return new ImportResult(totalRows, totalRows, 0, List.of());
    }

    public static ImportResult allFailed(int totalRows, List<RowError> errors) {
        return new ImportResult(totalRows, 0, totalRows, errors);
    }

    public boolean hasErrors() {
        return this.errorRows > 0;
    }

    public boolean isPerfect() {
        return this.errorRows == 0 && this.successRows == this.totalRows;
    }
}
