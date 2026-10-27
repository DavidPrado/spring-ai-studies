INSERT INTO user_roles (user_id, role_id)
VALUES (
    '0050bdd1-d2c4-4c6c-96aa-c66a5e744fd3', -- ID do admin@erpiasmart.com
    '9ee80de1-72a7-439b-a4e7-03fd148ba9c8'  -- ID da ROLE_SYSTEM_ADMIN
)
ON CONFLICT DO NOTHING;