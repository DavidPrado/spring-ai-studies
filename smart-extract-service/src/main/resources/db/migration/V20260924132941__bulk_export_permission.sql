INSERT INTO permissions (id, name, description, category)
SELECT gen_random_uuid(),
       'bulk:export',
       'Exportar dados em massa (CSV/Excel) — FEAT-B01',
       'BULK'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.name = 'bulk:export'
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE p.name = 'bulk:export'
  AND r.name IN (
      'SYSTEM_ADMIN',
      'ROLE_SYSTEM_ADMIN',
      'ADMIN',
      'ROLE_ADMIN'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id
        AND rp.permission_id = p.id
  );