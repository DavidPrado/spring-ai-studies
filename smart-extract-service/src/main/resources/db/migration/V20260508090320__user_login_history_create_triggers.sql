-- =====================================================
-- TRIGGERS para atualização automática de timestamps
-- Apenas para tabelas que não herdam do BaseEntity
-- =====================================================

-- Função para atualizar updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger apenas na tabela user_login_history
-- (As outras tabelas já têm isso via BaseEntity)
DROP TRIGGER IF EXISTS update_user_login_history_updated_at ON user_login_history;
CREATE TRIGGER update_user_login_history_updated_at
    BEFORE UPDATE ON user_login_history
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();