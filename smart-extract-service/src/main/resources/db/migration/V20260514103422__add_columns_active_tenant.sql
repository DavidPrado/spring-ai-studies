ALTER TABLE tenants ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE tenants ADD COLUMN blocked_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE tenants ADD COLUMN inactivation_reason VARCHAR(255);

-- Opcional: Garantir que o Tenant Admin esteja sempre ativo
UPDATE tenants SET active = TRUE WHERE id = '00000000-0000-0000-0000-000000000000';