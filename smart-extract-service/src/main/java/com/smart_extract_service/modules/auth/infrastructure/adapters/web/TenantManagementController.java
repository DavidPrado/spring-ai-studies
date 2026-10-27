package com.smart_extract_service.modules.auth.infrastructure.adapters.web;


import com.smart_extract_service.modules.auth.application.dto.TenantRequest;
import com.smart_extract_service.modules.auth.application.dto.TenantResponse;
import com.smart_extract_service.modules.auth.domain.ports.input.TenantUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/system/tenants")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN', 'SYSTEM_ADMIN') or hasRole('SYSTEM_ADMIN')")
public class TenantManagementController {


    private final TenantUseCase tenantUseCase;

    @GetMapping
    public ResponseEntity<Page<TenantResponse>> listAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            Pageable pageable){

        return ResponseEntity.ok(tenantUseCase.findAll(name, active,pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantUseCase.findById(id));
    }

    @PostMapping
    public ResponseEntity<TenantResponse> create(@RequestBody @Valid TenantRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantUseCase.createTenant(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID id, @RequestBody @Valid TenantRequest request){
        tenantUseCase.updateTenant(id, request);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatus(@PathVariable UUID id, @RequestBody TenantRequest request){
        tenantUseCase.updateStatus(id,request);
    }
}
