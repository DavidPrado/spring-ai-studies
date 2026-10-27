-- =====================================================
-- ALTER TABLE: permissions
-- Adicionando colunas para categorização
-- =====================================================

-- Adicionar colunas para permissions
ALTER TABLE permissions
    ADD COLUMN IF NOT EXISTS category VARCHAR(50);

-- Adicionar índices
CREATE INDEX IF NOT EXISTS idx_permissions_category ON permissions(category);
CREATE INDEX IF NOT EXISTS idx_permissions_name ON permissions(name);

-- Comentários
COMMENT ON COLUMN permissions.category IS 'Categoria da permissão (USER, ROLE, DOCUMENT, etc.)';