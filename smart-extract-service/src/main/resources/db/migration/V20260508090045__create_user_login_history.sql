-- =====================================================
-- CREATE TABLE: user_login_history
-- Tabela para histórico de tentativas de login
-- =====================================================

-- Criar tabela de histórico de login
CREATE TABLE IF NOT EXISTS user_login_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    login_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    successful BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- Constraints
    CONSTRAINT fk_user_login_history_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Criar índices para consultas eficientes
CREATE INDEX IF NOT EXISTS idx_login_history_user_id ON user_login_history(user_id);
CREATE INDEX IF NOT EXISTS idx_login_history_login_at ON user_login_history(login_at DESC);
CREATE INDEX IF NOT EXISTS idx_login_history_successful ON user_login_history(successful);
CREATE INDEX IF NOT EXISTS idx_login_history_user_successful ON user_login_history(user_id, successful, login_at DESC);
CREATE INDEX IF NOT EXISTS idx_login_history_ip_address ON user_login_history(ip_address);
CREATE INDEX IF NOT EXISTS idx_login_history_created_at ON user_login_history(created_at);

-- Comentários
COMMENT ON TABLE user_login_history IS 'Histórico de todas as tentativas de login dos usuários';
COMMENT ON COLUMN user_login_history.user_id IS 'ID do usuário que tentou login';
COMMENT ON COLUMN user_login_history.login_at IS 'Data e hora da tentativa de login';
COMMENT ON COLUMN user_login_history.ip_address IS 'Endereço IP de origem da tentativa';
COMMENT ON COLUMN user_login_history.user_agent IS 'User Agent do navegador/cliente';
COMMENT ON COLUMN user_login_history.successful IS 'Indica se a tentativa foi bem-sucedida';
COMMENT ON COLUMN user_login_history.failure_reason IS 'Motivo da falha (senha incorreta, conta bloqueada, etc.)';