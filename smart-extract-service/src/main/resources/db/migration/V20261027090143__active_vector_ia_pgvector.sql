-- 1. Habilita a extensão pgvector (se ainda não estiver habilitada)
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Adiciona a coluna de embedding na tabela de itens do catálogo
-- O nomic-embed-text gera vetores de 768 dimensões
ALTER TABLE catalog_items
    ADD COLUMN IF NOT EXISTS embedding vector(768);

-- 3. Cria o índice HNSW para busca por similaridade
-- HNSW (Hierarchical Navigable Small World) é o algoritmo mais rápido para vetores.
-- Ele permite buscar os "vizinhos mais próximos" em milissegundos.
CREATE INDEX IF NOT EXISTS idx_catalog_items_embedding_hnsw
    ON catalog_items
    USING hnsw (embedding vector_cosine_ops);