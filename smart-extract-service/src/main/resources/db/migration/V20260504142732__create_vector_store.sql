
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS tenant_knowledge (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    content TEXT,
    metadata JSONB,
    embedding vector(1536),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_knowledge_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX ON tenant_knowledge USING hnsw (embedding vector_cosine_ops);

ALTER TABLE tenant_knowledge ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_knowledge_isolation_policy ON tenant_knowledge
    USING (tenant_id = (SELECT current_setting('app.current_tenant')::uuid));