-- ============================================================================
-- SUPER ADMIN: Vincular ROLE_SYSTEM_ADMIN a TODAS as permissões do sistema
-- ============================================================================
--
-- PROPÓSITO:
--   Garantir que o perfil de Administrador do Sistema tenha acesso total
--   a todas as funcionalidades, incluindo as novas (bulk:import, bulk:export, etc.)
--
-- CARACTERÍSTICAS:
--   → Idempotente: pode ser executado múltiplas vezes sem duplicar vínculos
--   → Automático: detecta todas as permissões existentes dinamicamente
--   → Seguro: usa NOT EXISTS para evitar conflitos de chave única
-- ============================================================================

BEGIN;

-- 1. Vincula o ROLE_SYSTEM_ADMIN a TODAS as permissões existentes
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id AS role_id,
    p.id AS permission_id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ROLE_SYSTEM_ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);

-- 2. Verificação: Quantas permissões o ROLE_SYSTEM_ADMIN tem agora?
SELECT
    '=== TOTAL DE PERMISSÕES DO ROLE_SYSTEM_ADMIN ===' AS relatorio,
    COUNT(*) AS total_permissoes
FROM role_permissions rp
         JOIN roles r ON r.id = rp.role_id
WHERE r.name = 'ROLE_SYSTEM_ADMIN';

-- 3. Lista detalhada de todas as permissões vinculadas
SELECT
    '=== LISTA DETALHADA ===' AS relatorio,
    p.category,
    p.name AS permission_name,
    p.description
FROM role_permissions rp
         JOIN roles r ON r.id = rp.role_id
         JOIN permissions p ON p.id = rp.permission_id
WHERE r.name = 'ROLE_SYSTEM_ADMIN'
ORDER BY p.category, p.name;

COMMIT;