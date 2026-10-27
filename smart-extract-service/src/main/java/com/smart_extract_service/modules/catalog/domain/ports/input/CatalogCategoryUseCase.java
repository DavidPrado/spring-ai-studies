package com.smart_extract_service.modules.catalog.domain.ports.input;

import com.smart_extract_service.modules.catalog.application.dto.AttributeDefRequest;
import com.smart_extract_service.modules.catalog.application.dto.AttributeDefResponse;
import com.smart_extract_service.modules.catalog.application.dto.CategoryRequest;
import com.smart_extract_service.modules.catalog.application.dto.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CatalogCategoryUseCase {

    // Categorias
    CategoryResponse createCategory(UUID tenantId, CategoryRequest request);
    CategoryResponse updateCategory(UUID categoryId, UUID tenantId, CategoryRequest request);
    void deleteCategory(UUID categoryId, UUID tenantId);
    List<CategoryResponse> listTree(UUID tenantId, boolean activeOnly);
    Page<CategoryResponse> list(UUID tenantId, Map<String, String> filters, Pageable pageable);

    // Campos extras (defs)
    AttributeDefResponse saveDef(UUID tenantId, UUID categoryId, AttributeDefRequest request);
    List<AttributeDefResponse> listDefs(UUID categoryId, UUID tenantId);
    void deleteDef(UUID defId, UUID tenantId);

}
