INSERT INTO roles (id, tenant_id, name, is_system_default, description, is_editable)
VALUES
(gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'ROLE_SYSTEM_ADMIN', true, 'Acesso total ao gerenciamento do sistema e tenants', false);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE r.name = 'ROLE_SYSTEM_ADMIN'
AND u.email IN ('admin@erpiasmart.com', 'dev@erpiasmart.com');

UPDATE users
SET role = 'SYSTEM_ADMIN'
WHERE email IN ('admin@erpiasmart.com', 'dev@erpiasmart.com');