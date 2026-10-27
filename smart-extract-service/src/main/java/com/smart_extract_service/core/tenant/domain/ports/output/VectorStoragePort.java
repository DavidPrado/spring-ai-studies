package com.smart_extract_service.core.tenant.domain.ports.output;

import java.util.UUID;

public interface VectorStoragePort {
    void save(String text, UUID tenantId);
}
