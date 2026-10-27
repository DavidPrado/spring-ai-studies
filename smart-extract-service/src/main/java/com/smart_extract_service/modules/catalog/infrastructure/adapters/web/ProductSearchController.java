package com.smart_extract_service.modules.catalog.infrastructure.adapters.web;

import com.smart_extract_service.core.context.TenantContext;
import com.smart_extract_service.modules.catalog.application.usecases.ProductSemanticSearchService;
import com.smart_extract_service.modules.catalog.domain.model.CatalogItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/catalog/items/search")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSemanticSearchService searchService;

    @PostMapping("/semantic")
    public ResponseEntity<List<CatalogItem>> semanticSearch(
            @RequestBody Map<String, Object> request) {

        String query = (String) request.get("query");
        int topK = request.containsKey("topK")
                ? ((Number) request.get("topK")).intValue()
                : 5;

        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        UUID tenantId = TenantContext.getCurrentTenant();

        List<CatalogItem> results = searchService.semanticSearch(query, tenantId, topK);
        return ResponseEntity.ok(results);
    }
}
