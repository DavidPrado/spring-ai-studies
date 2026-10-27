package com.smart_extract_service.modules.catalog.application.usecases;


import com.smart_extract_service.modules.catalog.domain.model.CatalogItem;
import com.smart_extract_service.modules.catalog.domain.ports.output.CatalogItemRepositoryPort;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProductEmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final CatalogItemRepositoryPort  repositoryPort;

    public ProductEmbeddingService(EmbeddingModel embeddingModel, CatalogItemRepositoryPort repositoryPort) {
        this.embeddingModel = embeddingModel;
        this.repositoryPort = repositoryPort;
    }

    @Async
    @Transactional
    public void generateAndSaveEmbedding(UUID productId){
        CatalogItem product = repositoryPort.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + productId));

        String textToEmbed = product.getName();
        if(product.getDescription() != null && !product.getDescription().isBlank()){
            textToEmbed += " " + product.getDescription();
        }

        EmbeddingRequest request = new EmbeddingRequest(
                List.of(textToEmbed),
                null
        );

        EmbeddingResponse response = embeddingModel.call(request);
        List<Double> doubleList = response.getResult().getOutput();

        float[] embedding = new float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            embedding[i] = doubleList.get(i).floatValue();
        }

        repositoryPort.updateEmbedding(productId, embedding);
    }


    @Transactional
    public void generateEmbeddingsForAllProducts(UUID tenantId){

        var products = repositoryPort.findAll(tenantId, java.util.Map.of(),
                org.springframework.data.domain.Pageable.unpaged());

        int successCount = 0;
        int errorCount = 0;

        for(CatalogItem product : products.getContent()){
            try {
                generateAndSaveEmbedding(product.getId());
                successCount++;
            } catch (Exception e){
                log.error("❌ Erro ao gerar embedding para produto: {}", product.getId(), e);
                errorCount++;
            }
        }
        log.info("✅ Embeddings gerados: {} sucesso, {} erros", successCount, errorCount);
    }
}
