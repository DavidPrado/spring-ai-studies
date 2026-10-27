INSERT INTO permissions (id, name, description, category) VALUES
 (gen_random_uuid(), 'users:read',    'Listar e consultar usuários',   'USER'),
 (gen_random_uuid(), 'users:write',   'Criar e editar usuários',       'USER'),
 (gen_random_uuid(), 'users:delete',  'Excluir usuários',              'USER'),
 (gen_random_uuid(), 'roles:read',    'Consultar perfis de acesso',    'ROLE'),
 (gen_random_uuid(), 'roles:write',   'Criar e editar perfis',         'ROLE'),
 (gen_random_uuid(), 'roles:delete',  'Excluir perfis',                'ROLE'),
 (gen_random_uuid(), 'tenants:read',  'Consultar tenants',             'TENANT'),
 (gen_random_uuid(), 'tenants:write', 'Gerenciar tenants',             'TENANT')
ON CONFLICT (name) DO NOTHING;