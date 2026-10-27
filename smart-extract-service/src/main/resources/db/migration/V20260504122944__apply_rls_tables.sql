ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE role_permissions ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_users_isolation ON users
    USING (tenant_id = current_setting('app.current_tenant')::UUID);

CREATE POLICY tenant_roles_isolation ON roles
    USING (tenant_id = current_setting('app.current_tenant')::UUID);

CREATE POLICY tenant_user_roles_isolation ON user_roles
    USING (EXISTS (
        SELECT 1 FROM users
        WHERE users.id = user_roles.user_id
        AND users.tenant_id = current_setting('app.current_tenant')::UUID
    ));

CREATE POLICY tenant_role_permissions_isolation ON role_permissions
    USING (EXISTS (
        SELECT 1 FROM roles
        WHERE roles.id = role_permissions.role_id
        AND roles.tenant_id = current_setting('app.current_tenant')::UUID
    ));

ALTER TABLE users FORCE ROW LEVEL SECURITY;
ALTER TABLE roles FORCE ROW LEVEL SECURITY;