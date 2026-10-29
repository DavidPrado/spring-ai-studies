package com.smart_extract_service.modules.bulk.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RowErrorDetail {

    @JsonProperty("lineNumber")
    private int lineNumber;

    @JsonProperty("rowData")
    private Map<String, String> rowData;

    @JsonProperty("originalRow")
    private String originalRow;

    @JsonProperty("errorMessages")
    private List<String> errorMessages;

    @JsonProperty("errorCode")
    private String errorCode;

    public String extractKeyField() {
        if (rowData == null || rowData.isEmpty()) {
            return "N/A";
        }

        String sku = rowData.get("sku");
        if (sku != null && !sku.isBlank()) {
            return sku;
        }

        String ean = rowData.get("ean");
        if (ean != null && !ean.isBlank()) {
            return ean;
        }

        String name = rowData.get("name");
        if (name != null && !name.isBlank()) {
            return name.length() > 50 ? name.substring(0, 50) + "..." : name;
        }

        String id = rowData.get("id");
        if (id != null && !id.isBlank()) {
            return id;
        }

        return "N/A";
    }

    public String formattedErrors() {
        if (errorMessages == null || errorMessages.isEmpty()) {
            return "Erro não especificado";
        }
        return String.join(" | ", errorMessages);
    }

    public String getFirstError() {
        if (errorMessages == null || errorMessages.isEmpty()) {
            return "Erro desconhecido";
        }
        return errorMessages.get(0);
    }

    public boolean hasErrors() {
        return errorMessages != null && !errorMessages.isEmpty();
    }

    public String getField(String fieldName) {
        if (rowData == null) {
            return null;
        }
        return rowData.getOrDefault(fieldName, "");
    }

    public static RowErrorDetail fromRowError(RowError rowError, ObjectMapper objectMapper) {
        Map<String, String> parsedRowData = new HashMap<>();

        try {
            parsedRowData = objectMapper.readValue(
                    rowError.rowData(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}
            );
        } catch (Exception e) {
            parsedRowData.put("raw", rowError.rowData());
        }

        return RowErrorDetail.builder()
                .lineNumber(rowError.lineNumber())
                .rowData(parsedRowData)
                .originalRow(rowError.rowData())
                .errorMessages(rowError.errorMessages())
                .errorCode("VALIDATION_ERROR")
                .build();
    }
}