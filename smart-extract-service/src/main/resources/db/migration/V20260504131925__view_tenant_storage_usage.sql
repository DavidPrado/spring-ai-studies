CREATE OR REPLACE VIEW view_tenant_storage_usage AS
SELECT
    t.name AS tenant_name,
    t.id AS tenant_id,
    pg_size_pretty(SUM(
        (SELECT count(*) FROM users u WHERE u.tenant_id = t.id) * 2048 +
        (SELECT count(*) FROM roles r WHERE r.tenant_id = t.id) * 1024
    )::bigint) AS estimated_data_size,

    (SELECT count(*) FROM users u WHERE u.tenant_id = t.id) AS total_users,
    (SELECT count(*) FROM roles r WHERE r.tenant_id = t.id) AS total_roles
FROM
    tenants t
GROUP BY
    t.id, t.name;
