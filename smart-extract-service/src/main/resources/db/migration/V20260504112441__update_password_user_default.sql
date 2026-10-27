UPDATE users
SET password_hash = '$2a$10$Kntrpu1R8wEp5TEakuDge.AGb.NhfSO38kL25sN2Jn4Ym5YkCsIfW'
WHERE email IN (
    'dev@erpiasmart.com',
    'qa@erpiasmart.com',
    'support@erpiasmart.com',
    'admin@erpiasmart.com',
    'david@stefani.com'
);