-- =====================================================
-- ALTER TABLE: users
-- Adicionando novas colunas para dados cadastrais e segurança
-- =====================================================

-- Adicionar colunas para dados cadastrais
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20),
    ADD COLUMN IF NOT EXISTS department VARCHAR(100),
    ADD COLUMN IF NOT EXISTS position VARCHAR(100);

-- Adicionar colunas para segurança
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS last_login_ip VARCHAR(45),
    ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;

-- Adicionar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_users_full_name ON users(full_name);
CREATE INDEX IF NOT EXISTS idx_users_department ON users(department);
CREATE INDEX IF NOT EXISTS idx_users_last_login_at ON users(last_login_at);
CREATE INDEX IF NOT EXISTS idx_users_failed_attempts ON users(failed_login_attempts) WHERE locked_until IS NULL;

-- Comentários nas colunas
COMMENT ON COLUMN users.full_name IS 'Nome completo do usuário';
COMMENT ON COLUMN users.phone_number IS 'Número de telefone para contato';
COMMENT ON COLUMN users.department IS 'Departamento do usuário';
COMMENT ON COLUMN users.position IS 'Cargo/função do usuário';
COMMENT ON COLUMN users.last_login_at IS 'Data e hora do último login bem-sucedido';
COMMENT ON COLUMN users.last_login_ip IS 'Endereço IP do último login';
COMMENT ON COLUMN users.failed_login_attempts IS 'Número de tentativas de login falhas consecutivas';
COMMENT ON COLUMN users.locked_until IS 'Data até quando a conta está bloqueada';