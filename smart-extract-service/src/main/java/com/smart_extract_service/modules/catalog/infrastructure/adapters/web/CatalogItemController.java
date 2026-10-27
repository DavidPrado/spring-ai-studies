package com.smart_extract_service.modules.catalog.infrastructure.adapters.web;

import com.smart_extract_service.core.context.TenantContext;
import com.smart_extract_service.modules.catalog.application.dto.CatalogItemRequest;
import com.smart_extract_service.modules.catalog.application.dto.CatalogItemResponse;
import com.smart_extract_service.modules.catalog.application.dto.CatalogItemSummaryResponse;
import com.smart_extract_service.modules.catalog.application.dto.StatusUpdateRequest;
import com.smart_extract_service.modules.catalog.domain.ports.input.CatalogItemUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/catalog/items")
public class CatalogItemController {

    private final CatalogItemUseCase useCase;

    public CatalogItemController(CatalogItemUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('catalog:read')")
    public ResponseEntity<Page<CatalogItemSummaryResponse>> list(
            @RequestParam Map<String, String> filters,
            @PageableDefault(size = 20, sort = "name")Pageable pageable
            ){
        return ResponseEntity.ok(useCase.list(TenantContext.getCurrentTenant(), filters, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('catalog:read')")
    public ResponseEntity<CatalogItemResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(useCase.findById(id, TenantContext.getCurrentTenant()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('catalog:write')")
    public ResponseEntity<CatalogItemResponse> create(@RequestBody @Valid CatalogItemRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.createItem(TenantContext.getCurrentTenant(), request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('catalog:write')")
    public ResponseEntity<CatalogItemResponse> update(@PathVariable UUID id,
                                                      @RequestBody @Valid CatalogItemRequest request){
        return ResponseEntity.ok(useCase.updateItem(id, TenantContext.getCurrentTenant(), request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('catalog:write')")
    public ResponseEntity<CatalogItemResponse> changeStatus(@PathVariable UUID id,
                                                            @RequestBody @Valid StatusUpdateRequest  request){
        return  ResponseEntity.ok(
                useCase.changeStatus(id, TenantContext.getCurrentTenant(), request.status()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('catalog:delete')")
    public  ResponseEntity<Void> delete(@PathVariable UUID id){
        useCase.deleteItem(id, TenantContext.getCurrentTenant());
        return ResponseEntity.noContent().build();
    }
}
