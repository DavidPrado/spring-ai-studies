package com.smart_extract_service.core.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeService {
    private final VectorStore vectorStore;

    public void adicionarConhecimento(String texto, UUID tenantId) {
        Document doc = new Document(texto, Map.of("tenant_id", tenantId));
        vectorStore.add(List.of(doc));
    }
}
