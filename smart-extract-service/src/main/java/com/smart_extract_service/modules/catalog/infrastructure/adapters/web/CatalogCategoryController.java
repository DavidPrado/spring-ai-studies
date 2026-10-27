package com.smart_extract_service.modules.catalog.infrastructure.adapters.web;

import com.smart_extract_service.core.context.TenantContext;
import com.smart_extract_service.modules.catalog.application.dto.AttributeDefRequest;
import com.smart_extract_service.modules.catalog.application.dto.AttributeDefResponse;
import com.smart_extract_service.modules.catalog.application.dto.CategoryRequest;
import com.smart_extract_service.modules.catalog.application.dto.CategoryResponse;
import com.smart_extract_service.modules.catalog.domain.ports.input.CatalogCategoryUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/catalog/categories")
public class CatalogCategoryController {

    private final CatalogCategoryUseCase useCase;


    public CatalogCategoryController(CatalogCategoryUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('catalog:read')")
    public ResponseEntity<Page<CategoryResponse>> list(
            @RequestParam Map<String, String> filters,
            @PageableDefault(size = 50, sort = "name")Pageable pageable
            ){
        return ResponseEntity.ok(useCase.list(TenantContext.getCurrentTenant(), filters, pageable));
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('catalog:read')")
    public ResponseEntity<List<CategoryResponse>> tree(
            @RequestParam(defaultValue = "false") boolean activeOnly
    ){
        return ResponseEntity.ok(useCase.listTree(TenantContext.getCurrentTenant(), activeOnly));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('catalog:write')")
    public ResponseEntity<CategoryResponse> create(@RequestBody @Valid CategoryRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(useCase.createCategory(TenantContext.getCurrentTenant(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('catalog:write')")
    public ResponseEntity<CategoryResponse> update(@PathVariable UUID id,
                                                   @RequestBody @Valid CategoryRequest request){
        return ResponseEntity.ok(useCase.updateCategory(id, TenantContext.getCurrentTenant(),request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('catalog:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        useCase.deleteCategory(id, TenantContext.getCurrentTenant());
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/attributes")
    @PreAuthorize("hasAuthority('catalog:read')")
    public ResponseEntity<List<AttributeDefResponse>> listDefs(@PathVariable UUID id){
        return ResponseEntity.ok(useCase.listDefs(id, TenantContext.getCurrentTenant()));
    }

    @PostMapping("/{id}/attributes")
    @PreAuthorize("hasAuthority('catalog:write')")
    public ResponseEntity<AttributeDefResponse> saveDef(@PathVariable UUID id,
                                                        @RequestBody @Valid AttributeDefRequest request){
        return  ResponseEntity.status(HttpStatus.CREATED)
                .body(useCase.saveDef(TenantContext.getCurrentTenant(), id, request));
    }

    @DeleteMapping("/attributes/{defId}")
    @PreAuthorize("hasAuthority('catalog:write')")
    public ResponseEntity<Void> deleteDef(@PathVariable UUID defId){
        useCase.deleteDef(defId, TenantContext.getCurrentTenant());
        return ResponseEntity.noContent().build();
    }
}
