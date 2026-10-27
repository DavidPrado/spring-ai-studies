-- Remove a tabela que está incompleta
DROP TABLE IF EXISTS vector_store CASCADE;

-- Remove o registro de erro da tabela do Flyway
DELETE FROM flyway_schema_history WHERE version = '20260504191116';

-- 1. Garante extensões necessárias
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Cria a Função de Preenchimento de Tenant (se não existir)
CREATE OR REPLACE FUNCTION trg_set_tenant_id()
RETURNS TRIGGER AS $$
BEGIN
    -- Busca o tenant_id da sessão configurada no PostgreSQL
    NEW.tenant_id := current_setting('app.current_tenant')::uuid;
    RETURN NEW;
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Erro ao definir tenant_id: verifique se app.current_tenant esta configurado na sessao.';
END;
$$ LANGUAGE plpgsql;

-- 3. Limpeza de tabelas antigas para evitar conflitos de estrutura
DROP TABLE IF EXISTS tenant_knowledge CASCADE;
DROP TABLE IF EXISTS vector_store CASCADE;

-- 4. Criação da tabela oficial do Spring AI com suporte a Multi-tenant
CREATE TABLE vector_store (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id uuid NOT NULL,
    content text,
    metadata jsonb,
    embedding vector(768)
);

-- 5. Configuração de Segurança (RLS)
ALTER TABLE vector_store ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS tenant_isolation_policy ON vector_store;

CREATE POLICY tenant_isolation_policy ON vector_store
    USING (tenant_id = (SELECT current_setting('app.current_tenant')::uuid));

-- 6. Criação da Trigger vinculada à função criada no item 2
DROP TRIGGER IF EXISTS tgr_fill_tenant_id ON vector_store;

CREATE TRIGGER tgr_fill_tenant_id
BEFORE INSERT ON vector_store
FOR EACH ROW EXECUTE FUNCTION trg_set_tenant_id();