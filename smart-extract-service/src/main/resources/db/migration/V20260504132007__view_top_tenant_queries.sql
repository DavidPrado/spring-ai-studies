CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

CREATE OR REPLACE VIEW view_top_tenant_queries AS
SELECT
    substring(query from '/\* tenant_id: (.*) \*/') AS tenant_id,
    query,
    calls,
    total_exec_time / 1000 AS total_exec_seconds,
    mean_exec_time AS average_ms,
    rows
FROM
    pg_stat_statements
WHERE
    query LIKE '%/* tenant_id:%'
ORDER BY
    total_exec_time DESC
LIMIT 20;