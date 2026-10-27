SET app.current_tenant = '00000000-0000-0000-0000-000000000000';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.email = 'admin@erpiasmart.com'
  AND r.name = 'ROLE_SYSTEM_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM user_roles ur
        JOIN users u ON u.id = ur.user_id
        JOIN roles r ON r.id = ur.role_id
        WHERE u.email = 'admin@erpiasmart.com'
          AND r.name = 'ROLE_SYSTEM_ADMIN'
    ) THEN
        RAISE EXCEPTION 'SEED FAILED: admin@erpiasmart.com sem ROLE_SYSTEM_ADMIN';
    END IF;
END $$;

RESET app.current_tenant;