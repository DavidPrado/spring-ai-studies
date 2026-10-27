-- =====================================================
-- ALTER TABLE: roles
-- Adicionando colunas para flexibilidade
-- =====================================================

-- Adicionar colunas para roles
ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS is_editable BOOLEAN DEFAULT TRUE;

-- Modificar constraint unique para permitir mesmo nome em diferentes tenants
ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_tenant_id_name_key;
ALTER TABLE roles ADD CONSTRAINT roles_tenant_id_name_unique UNIQUE(tenant_id, name);

-- Adicionar índices
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);
CREATE INDEX IF NOT EXISTS idx_roles_is_system_default ON roles(is_system_default);

-- Comentários
COMMENT ON COLUMN roles.description IS 'Descrição do papel/função';
COMMENT ON COLUMN roles.is_editable IS 'Indica se o papel pode ser editado pelo tenant';
COMMENT ON COLUMN roles.is_system_default IS 'Indica se é um papel padrão do sistema';