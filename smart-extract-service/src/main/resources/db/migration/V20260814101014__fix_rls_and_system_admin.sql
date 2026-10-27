DROP POLICY IF EXISTS tenant_users_isolation ON users;
CREATE POLICY tenant_users_isolation ON users
USING (
    tenant_id = current_setting('app.current_tenant', true)::uuid
    OR current_setting('app.current_tenant', true) = '00000000-0000-0000-0000-000000000000'
);

DROP POLICY IF EXISTS tenant_roles_isolation ON roles;
CREATE POLICY tenant_roles_isolation ON roles
USING (
    tenant_id = current_setting('app.current_tenant', true)::uuid
    OR is_system_default = true
    OR current_setting('app.current_tenant', true) = '00000000-0000-0000-0000-000000000000'
);

DROP POLICY IF EXISTS tenant_user_roles_isolation ON user_roles;
CREATE POLICY tenant_user_roles_isolation ON user_roles
USING (EXISTS (
    SELECT 1 FROM users u WHERE u.id = user_roles.user_id
));

DROP POLICY IF EXISTS tenant_role_permissions_isolation ON role_permissions;
CREATE POLICY tenant_role_permissions_isolation ON role_permissions
USING (EXISTS (
    SELECT 1 FROM roles r WHERE r.id = role_permissions.role_id
));

ALTER TABLE tenants ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS tenant_visibility ON tenants;
CREATE POLICY tenant_visibility ON tenants
USING (
    id = current_setting('app.current_tenant', true)::uuid
    OR current_setting('app.current_tenant', true) = '00000000-0000-0000-0000-000000000000'
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u CROSS JOIN roles r
WHERE u.email = 'admin@erpiasmart.com'
  AND r.name = 'ROLE_SYSTEM_ADMIN'
  AND r.tenant_id = '00000000-0000-0000-0000-000000000000'
ON CONFLICT (user_id, role_id) DO NOTHING;

UPDATE users
SET active = true, require_password_change = false
WHERE email = 'admin@erpiasmart.com';
