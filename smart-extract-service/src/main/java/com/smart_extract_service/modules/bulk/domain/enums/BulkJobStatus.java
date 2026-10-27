package com.smart_extract_service.modules.bulk.domain.enums;

public enum BulkJobStatus {
    QUEUED,
    PROCESSING,
    CANCELLING,
    CANCELLED,
    DONE,
    PARTIAL,
    FAILED;


    public boolean isTerminal(){
        return
                this == DONE || this == FAILED || this == CANCELLED;
    }

    public boolean isCancellable() {
        return this == QUEUED || this == PROCESSING;
    }
}
