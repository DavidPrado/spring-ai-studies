package com.smart_extract_service.modules.catalog.application.usecases;


import com.smart_extract_service.modules.catalog.domain.model.CatalogItem;
import com.smart_extract_service.modules.catalog.domain.ports.output.CatalogItemRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSemanticSearchService {

    private final EmbeddingModel embeddingModel;
    private final CatalogItemRepositoryPort repository;

    @Transactional(readOnly = true)
    public List<CatalogItem> semanticSearch(String query, UUID tenantId, int topK) {
        log.info("🔍 [BUSCA SEMÂNTICA] Query: '{}' | Tenant: {} | TopK: {}", query, tenantId, topK);

        EmbeddingRequest request = new EmbeddingRequest(List.of(query), null);
        EmbeddingResponse response = embeddingModel.call(request);

        List<Double> doubleList = response.getResult().getOutput();

        float[] queryEmbedding = new float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            queryEmbedding[i] = doubleList.get(i).floatValue();
        }

        log.info("📐 [BUSCA SEMÂNTICA] Embedding gerado com {} dimensões", queryEmbedding.length);

        List<CatalogItem> results = repository.findBySemanticSimilarity(tenantId, queryEmbedding, topK, 0.7);

        log.info("✅ [BUSCA SEMÂNTICA] Encontrados {} produtos similares", results.size());

        return results;
    }

    public List<CatalogItem> semanticSearch(String query, UUID tenantId) {
        return semanticSearch(query, tenantId, 5);
    }
}
