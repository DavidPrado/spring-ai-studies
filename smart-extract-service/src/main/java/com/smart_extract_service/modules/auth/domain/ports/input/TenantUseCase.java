package com.smart_extract_service.modules.auth.domain.ports.input;

import com.smart_extract_service.modules.auth.application.dto.TenantRequest;
import com.smart_extract_service.modules.auth.application.dto.TenantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TenantUseCase {
    TenantResponse createTenant(TenantRequest request);
    void updateTenant(UUID id, TenantRequest request);
    void updateStatus(UUID id, TenantRequest request);
    Page<TenantResponse> findAll(String name , Boolean active,Pageable pageable);
    TenantResponse findById(UUID id);
}
