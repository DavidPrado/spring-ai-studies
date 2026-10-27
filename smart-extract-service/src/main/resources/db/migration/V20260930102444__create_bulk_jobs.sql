-- ============================================================================
-- FEAT-B03/B04: Tabela bulk_jobs (fila de processamento + histórico)
-- ============================================================================
--
-- PROPÓSITO:
--   Esta tabela serve DUPLA função:
--   1. FILA: jobs com status 'QUEUED' aguardam processamento
--   2. HISTÓRICO: jobs concluídos ficam aqui para auditoria
--
-- POR QUE UMA TABELA SÓ?
--   → Simplicidade: uma query para "meus jobs"
--   → Performance: índice único cobre ambos os casos
--   → Evita sincronização entre tabela de fila e tabela de histórico
--
-- DECISÕES DE DESIGN:
--   - payload_json (JSONB): armazena até ~5MB de dados inline
--   - payload_file_path: para arquivos grandes (>5MB), salvos em disco
--   - Advisory Locks: previnem que 2 workers processem o mesmo job
--   - RLS: cada tenant vê apenas seus próprios jobs
-- ============================================================================

CREATE TABLE IF NOT EXISTS bulk_jobs (
    -- Identidade
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Isolamento multi-tenant (RLS)
    tenant_id UUID NOT NULL,

    -- Quem criou (para painel do usuário)
    user_id UUID NOT NULL,
    username VARCHAR(100) NOT NULL,

    -- O que está sendo processado
    entity VARCHAR(100) NOT NULL,           -- ex: 'catalog-items', 'parties'
    operation VARCHAR(20) NOT NULL,         -- 'IMPORT_JSON', 'IMPORT_CSV', 'EXPORT_CSV'
    mode VARCHAR(20),                       -- 'INSERT_ONLY' ou 'UPSERT'

    -- Estado do job
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',

    -- Progresso (atualizado a cada lote)
    total_items INT NOT NULL DEFAULT 0,
    processed_items INT NOT NULL DEFAULT 0,
    success_items INT NOT NULL DEFAULT 0,
    error_items INT NOT NULL DEFAULT 0,

    -- Payload (dados a processar)
    payload_type VARCHAR(10) NOT NULL,      -- 'JSON' ou 'FILE'
    payload_json JSONB,                     -- Dados inline (até ~5MB)
    payload_file_path TEXT,                 -- Caminho no disco (para arquivos grandes)

    -- Resultado
    error_file_path TEXT,                   -- CSV de erros (gerado ao final)
    error_summary JSONB,                    -- Resumo dos erros (top 100)

    -- Controle de execução
    worker_id VARCHAR(100),                 -- Qual worker está processando (debug)
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    last_error TEXT,                        -- Última mensagem de erro

    -- Timestamps
    queued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================================
-- ÍNDICES (otimização de queries)
-- ============================================================================

-- Índice 1: Painel do usuário ("meus jobs mais recentes")
-- Cobertura: tenant + usuário + data decrescente
CREATE INDEX idx_bulk_jobs_user_recent
    ON bulk_jobs(tenant_id, user_id, created_at DESC);

-- Índice 2: Worker buscando jobs para processar
-- Cobertura: status 'QUEUED' ordenado por antiguidade (FIFO)
CREATE INDEX idx_bulk_jobs_queued
    ON bulk_jobs(status, queued_at ASC)
    WHERE status = 'QUEUED';  -- Índice parcial (só QUEUED, mais rápido)

-- Índice 3: Limpeza automática (jobs antigos > 90 dias)
CREATE INDEX idx_bulk_jobs_created
    ON bulk_jobs(created_at ASC);

-- ============================================================================
-- TRIGGER: Atualiza updated_at automaticamente em cada UPDATE
-- ============================================================================
CREATE OR REPLACE FUNCTION fn_bulk_jobs_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_bulk_jobs_updated_at
    BEFORE UPDATE ON bulk_jobs
    FOR EACH ROW
    EXECUTE FUNCTION fn_bulk_jobs_updated_at();

-- ============================================================================
-- SEED: Permissão RBAC para importação
-- ============================================================================
INSERT INTO permissions (id, name, description, category)
SELECT gen_random_uuid(), 'bulk:import', 'Importar dados em massa (CSV/JSON)', 'BULK'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'bulk:import');

-- Vincula a permissão aos perfis admin
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE p.name = 'bulk:import'
  AND r.name IN ('SYSTEM_ADMIN', 'ADMIN')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );