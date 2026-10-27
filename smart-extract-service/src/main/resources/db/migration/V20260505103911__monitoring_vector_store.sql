
ALTER TABLE vector_store
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

CREATE OR REPLACE VIEW view_monitoramento_armazenamento_ia AS
SELECT
    tenant_id,
    count(*) as total_fragmentos,
    pg_size_pretty(sum(pg_column_size(embedding) + pg_column_size(content))) as tamanho_estimado,
    MAX(created_at) as ultima_atualizacao
FROM vector_store
GROUP BY tenant_id;

CREATE OR REPLACE VIEW view_alerta_ia_lenta AS
SELECT
    query,
    calls,
    round(total_exec_time::numeric / calls::numeric, 2) as media_ms,
    rows / calls as media_linhas_retornadas
FROM pg_stat_statements
WHERE query LIKE '%vector_store%'
  AND calls > 0
  AND (total_exec_time / calls) > 1000
ORDER BY media_ms DESC;