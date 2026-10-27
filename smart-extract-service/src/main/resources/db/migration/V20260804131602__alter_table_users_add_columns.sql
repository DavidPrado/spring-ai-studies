ALTER TABLE users
    ADD COLUMN IF NOT EXISTS phone2 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS cost_center VARCHAR(50),
    ADD COLUMN IF NOT EXISTS employee_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS last_password_change_at TIMESTAMP WITH TIME ZONE;

COMMENT ON COLUMN users.phone2 IS 'Telefone secundário';
COMMENT ON COLUMN users.cost_center IS 'Centro de custo (alocação financeira)';
COMMENT ON COLUMN users.employee_code IS 'Código interno do RH';
COMMENT ON COLUMN users.last_password_change_at IS 'Data da última alteração de senha';