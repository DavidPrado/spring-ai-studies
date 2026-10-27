DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'phone2'
    ) THEN
        ALTER TABLE users RENAME COLUMN phone2 TO phone_number2;
    END IF;
END $$;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_temporary BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS password_change_interval_days INT NOT NULL DEFAULT 90;

COMMENT ON COLUMN users.id IS 'Identificador único do usuário (UUID)';
COMMENT ON COLUMN users.tenant_id IS 'Identificador do Tenant pertencente (UUID)';
COMMENT ON COLUMN users.email IS 'E-mail principal para login e comunicação';
COMMENT ON COLUMN users.password_hash IS 'Hash da senha codificada com BCrypt/Argon2';
COMMENT ON COLUMN users.role IS 'Perfil/Papel de acesso no sistema';
COMMENT ON COLUMN users.created_at IS 'Data e hora de criação do registro';
COMMENT ON COLUMN users.active IS 'Indica se a conta está ativa ou inativa para login';
COMMENT ON COLUMN users.require_password_change IS 'Indica se o usuário deve obrigatoriamente alterar a senha no próximo acesso';
COMMENT ON COLUMN users.expires_at IS 'Data e hora limite de expiração do acesso (Acesso Temporário)';
COMMENT ON COLUMN users.created_by IS 'ID do usuário que criou este registro';
COMMENT ON COLUMN users.updated_at IS 'Data e hora da última modificação do registro';
COMMENT ON COLUMN users.updated_by IS 'ID do usuário que realizou a última alteração';
COMMENT ON COLUMN users.full_name IS 'Nome completo do usuário';
COMMENT ON COLUMN users.phone_number IS 'Número de telefone/celular principal';
COMMENT ON COLUMN users.phone_number2 IS 'Número de telefone/celular secundário ou recado';
COMMENT ON COLUMN users.department IS 'Departamento do usuário na empresa';
COMMENT ON COLUMN users.position IS 'Cargo ou função do usuário';
COMMENT ON COLUMN users.last_login_at IS 'Data e hora do último login bem-sucedido';
COMMENT ON COLUMN users.last_login_ip IS 'Endereço IP utilizado no último login';
COMMENT ON COLUMN users.failed_login_attempts IS 'Contador de tentativas consecutivas incorretas de login';
COMMENT ON COLUMN users.locked_until IS 'Data e hora até a qual a conta permanece bloqueada por segurança';
COMMENT ON COLUMN users.cost_center IS 'Centro de custo do colaborador (alocação financeira)';
COMMENT ON COLUMN users.employee_code IS 'Matrícula/Código interno do colaborador no RH';
COMMENT ON COLUMN users.last_password_change_at IS 'Data e hora da última alteração de senha efetuada';
COMMENT ON COLUMN users.is_temporary IS 'Flag para identificar se o acesso do usuário é temporário';
COMMENT ON COLUMN users.password_change_interval_days IS 'Quantidade máxima de dias de validade da senha antes de expirar';

CREATE TABLE IF NOT EXISTS user_password_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_password_history_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE user_password_history IS 'Tabela de histórico de senhas antigas para impedir reuso das últimas 5 senhas';
COMMENT ON COLUMN user_password_history.id IS 'Identificador único do registro de histórico';
COMMENT ON COLUMN user_password_history.user_id IS 'ID do usuário proprietário do histórico de senha';
COMMENT ON COLUMN user_password_history.password_hash IS 'Hash da senha antiga armazenada';
COMMENT ON COLUMN user_password_history.created_at IS 'Data de gravação do hash no histórico';

CREATE INDEX IF NOT EXISTS idx_user_password_history_user_date
    ON user_password_history(user_id, created_at DESC);