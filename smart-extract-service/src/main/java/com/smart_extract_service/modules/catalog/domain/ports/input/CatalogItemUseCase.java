package com.smart_extract_service.modules.catalog.domain.ports.input;

import com.smart_extract_service.modules.catalog.application.dto.CatalogItemRequest;
import com.smart_extract_service.modules.catalog.application.dto.CatalogItemResponse;
import com.smart_extract_service.modules.catalog.application.dto.CatalogItemSummaryResponse;
import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface CatalogItemUseCase {
    CatalogItemResponse createItem(UUID tenantId, CatalogItemRequest request);
    CatalogItemResponse updateItem(UUID itemId, UUID tenantId, CatalogItemRequest request);
    CatalogItemResponse findById(UUID itemId, UUID tenantId);
    Page<CatalogItemSummaryResponse> list(UUID tenantId, Map<String, String> filters, Pageable pageable);
    CatalogItemResponse changeStatus(UUID itemId, UUID tenantId, ItemStatus target);
    void deleteItem(UUID itemId, UUID tenantId);
}
