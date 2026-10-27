package com.smart_extract_service.core.exception;

public record ErrorResponse(int status, String message, long timestamp) {
}
