ALTER TABLE roles ADD COLUMN IF NOT EXISTS display_name VARCHAR(100);

COMMENT ON COLUMN roles.display_name
    IS 'Rótulo amigável exibido na UI; o nome técnico permanece em name';

UPDATE roles
SET display_name = CASE name
    WHEN 'ROLE_ADMIN'         THEN 'Administrador'
    WHEN 'ROLE_DEV'           THEN 'Desenvolvedor'
    WHEN 'ROLE_QA'            THEN 'Analista de Qualidade (QA)'
    WHEN 'ROLE_SUPPORT'       THEN 'Suporte'
    WHEN 'ROLE_SYSTEM_ADMIN'  THEN 'Administrador do Sistema'
    ELSE initcap(replace(lower(substring(name from 6)), '_', ' '))
END
WHERE display_name IS NULL
   OR display_name LIKE 'ROLE_%';