INSERT INTO tenants (id, name, subdomain, country, currency_code)
VALUES ('00000000-0000-0000-0000-000000000000', 'Smart Extract Admin', 'admin', 'BRA', 'BRL');

INSERT INTO roles (id, tenant_id, name, is_system_default) VALUES
(gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'ROLE_ADMIN', true),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'ROLE_DEV', true),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'ROLE_QA', true),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'ROLE_SUPPORT', true);

INSERT INTO users (id, tenant_id, email, password_hash, role) VALUES
(gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'admin@erpiasmart.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ADMIN'),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'dev@erpiasmart.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'DEV'),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'qa@erpiasmart.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'QA'),
(gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'support@erpiasmart.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'SUPPORT');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@erpiasmart.com' AND r.name = 'ROLE_ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'dev@erpiasmart.com' AND r.name = 'ROLE_DEV';