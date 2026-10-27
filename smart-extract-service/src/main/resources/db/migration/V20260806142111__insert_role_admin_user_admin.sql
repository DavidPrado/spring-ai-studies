INSERT INTO user_roles (user_id, role_id)
SELECT
    '0050bdd1-d2c4-4c6c-96aa-c66a5e744fd3',
    id
FROM roles
WHERE name = 'ROLE_SYSTEM_ADMIN';