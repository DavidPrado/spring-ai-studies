-- ============================================================================
-- MÓDULO: smart-catalog — Catálogo unificado de Produtos e Serviços
-- ----------------------------------------------------------------------------
-- Conteúdo:
--   1. catalog_categories      → mapa de seções (árvore)
--   2. catalog_attribute_defs  → perguntas/campos extras de cada seção
--   3. catalog_items           → ficha do item (produto OU serviço)
--   4. catalog_item_variants   → versões comerciais (grade tamanho/cor/embalagem)
--   5. RLS (isolamento por tenant)
--   6. Seeds de permissões catalog:*
-- Obs.: toda coluna possui COMMENT em português (consultável no DBeaver em
--       Propriedades → Comentários, ou via pg_description).
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS vector;   -- pgvector (busca semântica da IA)

-- ============================================================================
-- 1) CATEGORIAS — o mapa de seções do catálogo
-- ============================================================================
CREATE TABLE catalog_categories (
    id         UUID         PRIMARY KEY,
    tenant_id  UUID         NOT NULL REFERENCES tenants(id),
    parent_id  UUID         REFERENCES catalog_categories(id),
    name       VARCHAR(120) NOT NULL,
    vertical   VARCHAR(20)  NOT NULL DEFAULT 'GENERIC',
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by UUID,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    CONSTRAINT ck_category_vertical CHECK (vertical IN
        ('GENERIC','SERVICE','SANITARY','HAZARDOUS','APPAREL','CONSTRUCTION'))
);

COMMENT ON TABLE  catalog_categories IS 'Seções e subseções do catálogo em árvore (ex.: Medicamentos > Analgésicos).';
COMMENT ON COLUMN catalog_categories.id IS 'Identificador único da seção.';
COMMENT ON COLUMN catalog_categories.tenant_id IS 'Tenant dono do registro (isolamento RLS).';
COMMENT ON COLUMN catalog_categories.parent_id IS 'Seção pai. NULO = seção raiz da árvore.';
COMMENT ON COLUMN catalog_categories.name IS 'Nome de exibição da seção (ex.: "Medicamentos").';
COMMENT ON COLUMN catalog_categories.vertical IS 'Família do negócio (GENERIC|SERVICE|SANITARY|HAZARDOUS|APPAREL|CONSTRUCTION). Dirige quais campos extras o formulário mostra.';
COMMENT ON COLUMN catalog_categories.active IS 'Seção ativa? Inativa não aparece em novos cadastros.';
COMMENT ON COLUMN catalog_categories.created_at IS 'Data/hora de criação do registro.';
COMMENT ON COLUMN catalog_categories.created_by IS 'Usuário que criou o registro.';
COMMENT ON COLUMN catalog_categories.updated_at IS 'Data/hora da última alteração.';
COMMENT ON COLUMN catalog_categories.updated_by IS 'Usuário que fez a última alteração.';

CREATE INDEX idx_catcat_tenant_active ON catalog_categories(tenant_id, active);

-- ============================================================================
-- 2) DEFINIÇÕES DE ATRIBUTOS — as "perguntas" que cada seção faz
-- ============================================================================
CREATE TABLE catalog_attribute_defs (
    id          UUID         PRIMARY KEY,
    tenant_id   UUID         NOT NULL REFERENCES tenants(id),
    category_id UUID         NOT NULL REFERENCES catalog_categories(id) ON DELETE CASCADE,
    key         VARCHAR(60)  NOT NULL,
    label       VARCHAR(120) NOT NULL,
    data_type   VARCHAR(20)  NOT NULL,
    unit        VARCHAR(10),
    options     JSONB,
    required    BOOLEAN      NOT NULL DEFAULT FALSE,
    applies_to  VARCHAR(10)  NOT NULL DEFAULT 'BOTH',
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  UUID,
    updated_at  TIMESTAMPTZ,
    updated_by  UUID,
    CONSTRAINT ck_def_data_type  CHECK (data_type IN ('TEXT','NUMBER','DATE','BOOLEAN','ENUM')),
    CONSTRAINT ck_def_applies_to CHECK (applies_to IN ('PRODUCT','SERVICE','BOTH')),
    CONSTRAINT uq_def_key_cat    UNIQUE (category_id, key)
);

COMMENT ON TABLE  catalog_attribute_defs IS 'Campos extras definidos por seção (o "formulário em branco"). As respostas ficam em catalog_items.extra_data.';
COMMENT ON COLUMN catalog_attribute_defs.id IS 'Identificador único da definição.';
COMMENT ON COLUMN catalog_attribute_defs.tenant_id IS 'Tenant dono do registro (isolamento RLS).';
COMMENT ON COLUMN catalog_attribute_defs.category_id IS 'Seção à qual este campo extra pertence.';
COMMENT ON COLUMN catalog_attribute_defs.key IS 'Chave técnica gravada no extra_data do item (ex.: "registry_number"). Sem espaços/acentos.';
COMMENT ON COLUMN catalog_attribute_defs.label IS 'Rótulo amigável exibido no formulário (ex.: "Registro ANVISA").';
COMMENT ON COLUMN catalog_attribute_defs.data_type IS 'Tipo do campo: TEXT | NUMBER | DATE | BOOLEAN | ENUM.';
COMMENT ON COLUMN catalog_attribute_defs.unit IS 'Unidade de medida do valor, quando aplicável (kg, cm, mL).';
COMMENT ON COLUMN catalog_attribute_defs.options IS 'Valores permitidos quando data_type = ENUM (ex.: ["Oral","Injetável"]).';
COMMENT ON COLUMN catalog_attribute_defs.required IS 'Preenchimento obrigatório para itens desta seção?';
COMMENT ON COLUMN catalog_attribute_defs.applies_to IS 'Aplica-se a: PRODUCT | SERVICE | BOTH.';
COMMENT ON COLUMN catalog_attribute_defs.sort_order IS 'Ordem de exibição do campo no formulário.';
COMMENT ON COLUMN catalog_attribute_defs.created_at IS 'Data/hora de criação do registro.';
COMMENT ON COLUMN catalog_attribute_defs.created_by IS 'Usuário que criou o registro.';
COMMENT ON COLUMN catalog_attribute_defs.updated_at IS 'Data/hora da última alteração.';
COMMENT ON COLUMN catalog_attribute_defs.updated_by IS 'Usuário que fez a última alteração.';

CREATE INDEX idx_def_category ON catalog_attribute_defs(category_id, sort_order);

-- ============================================================================
-- 3) ITENS — a ficha do produto ou serviço (coração do catálogo)
-- ============================================================================
CREATE TABLE catalog_items (
    id              UUID          PRIMARY KEY,
    tenant_id       UUID          NOT NULL REFERENCES tenants(id),
    category_id     UUID          NOT NULL REFERENCES catalog_categories(id),
    item_type       VARCHAR(10)   NOT NULL,
    status          VARCHAR(10)   NOT NULL DEFAULT 'DRAFT',
    sku             VARCHAR(40)   NOT NULL,
    ean             VARCHAR(14),
    name            VARCHAR(180)  NOT NULL,
    description     TEXT,
    unit_of_measure VARCHAR(5)    NOT NULL,
    sale_price      NUMERIC(15,2) NOT NULL DEFAULT 0,
    cost_price      NUMERIC(15,2) NOT NULL DEFAULT 0,
    ncm             VARCHAR(10),
    cest            VARCHAR(10),
    tax_origin      VARCHAR(2),
    cst_icms        VARCHAR(3),
    csosn           VARCHAR(3),
    service_code    VARCHAR(10),
    iss_rate        NUMERIC(5,2),
    tracks_batch    BOOLEAN       NOT NULL DEFAULT FALSE,
    tracks_expiry   BOOLEAN       NOT NULL DEFAULT FALSE,
    is_hazardous    BOOLEAN       NOT NULL DEFAULT FALSE,
    extra_data      JSONB         NOT NULL DEFAULT '{}',
    min_stock       INT,
    max_stock       INT,
    reorder_stock   INT,
    embedding       vector(768),
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by      UUID,
    updated_at      TIMESTAMPTZ,
    updated_by      UUID,
    CONSTRAINT ck_item_type   CHECK (item_type IN ('PRODUCT','SERVICE')),
    CONSTRAINT ck_item_status CHECK (status IN ('DRAFT','ACTIVE','INACTIVE')),
    CONSTRAINT ck_item_prices CHECK (sale_price >= 0 AND cost_price >= 0),
    CONSTRAINT uq_item_sku    UNIQUE (tenant_id, sku),
    CONSTRAINT uq_item_ean    UNIQUE (tenant_id, ean)
);

COMMENT ON TABLE  catalog_items IS 'Ficha única de cada item comercializável (produto físico ou serviço) do tenant.';
COMMENT ON COLUMN catalog_items.id IS 'Identificador único do item.';
COMMENT ON COLUMN catalog_items.tenant_id IS 'Tenant dono do registro (isolamento RLS).';
COMMENT ON COLUMN catalog_items.category_id IS 'Seção do catálogo à qual o item pertence.';
COMMENT ON COLUMN catalog_items.item_type IS 'Tipo do item: PRODUCT (físico, com estoque) | SERVICE (serviço, sem estoque).';
COMMENT ON COLUMN catalog_items.status IS 'Ciclo de vida: DRAFT (rascunho) | ACTIVE (ativo p/ vendas) | INACTIVE (desativado).';
COMMENT ON COLUMN catalog_items.sku IS 'Código interno único por tenant (ex.: PRD-000123).';
COMMENT ON COLUMN catalog_items.ean IS 'Código de barras EAN-13/14 (opcional). Dígito verificador validado na aplicação.';
COMMENT ON COLUMN catalog_items.name IS 'Nome comercial exibido em telas e documentos.';
COMMENT ON COLUMN catalog_items.description IS 'Descrição completa/comercial do item.';
COMMENT ON COLUMN catalog_items.unit_of_measure IS 'Unidade de medida: UN, KG, G, L, ML, M, M2, M3, HR, CX, PC.';
COMMENT ON COLUMN catalog_items.sale_price IS 'Preço de venda de tabela (moeda do tenant).';
COMMENT ON COLUMN catalog_items.cost_price IS 'Preço de custo de referência (margem é calculada, não gravada).';
COMMENT ON COLUMN catalog_items.ncm IS 'Nomenclatura Comum do Mercosul (classificação fiscal da mercadoria).';
COMMENT ON COLUMN catalog_items.cest IS 'Código Especificador da Substituição Tributária (ST).';
COMMENT ON COLUMN catalog_items.tax_origin IS 'Origem da mercadoria p/ ICMS (0 = nacional, 1 = estrangeira...).';
COMMENT ON COLUMN catalog_items.cst_icms IS 'Código de Situação Tributária do ICMS (regimes normais).';
COMMENT ON COLUMN catalog_items.csosn IS 'Código de Situação da Operação no Simples Nacional.';
COMMENT ON COLUMN catalog_items.service_code IS 'Código do serviço na Lista da LC 116/2003 (somente SERVICE, ex.: 7.02).';
COMMENT ON COLUMN catalog_items.iss_rate IS 'Alíquota de ISS aplicável ao serviço (%).';
COMMENT ON COLUMN catalog_items.tracks_batch IS 'Controla lote? (farmácia, químicos, alimentos). Liga rastreio no estoque.';
COMMENT ON COLUMN catalog_items.tracks_expiry IS 'Controla validade? Liga alerta de vencimento no estoque.';
COMMENT ON COLUMN catalog_items.is_hazardous IS 'É perigoso? Liga regras especiais de transporte/armazenagem.';
COMMENT ON COLUMN catalog_items.extra_data IS 'Dados extras da seção (JSONB): o "formulário preenchido" definido em catalog_attribute_defs. Ex.: {"registry_number":"1.0235-8"}.';
COMMENT ON COLUMN catalog_items.min_stock IS 'Estoque mínimo desejado (abaixo disso = alerta).';
COMMENT ON COLUMN catalog_items.max_stock IS 'Estoque máximo (teto da sugestão de compra).';
COMMENT ON COLUMN catalog_items.reorder_stock IS 'Ponto de pedido: saldo que dispara sugestão de reposição.';
COMMENT ON COLUMN catalog_items.embedding IS 'Vetor semântico (768 dimensões) p/ busca por linguagem natural e detecção de duplicados (IA).';
COMMENT ON COLUMN catalog_items.deleted_at IS 'Exclusão lógica (soft delete): data/hora da exclusão. NULO = registro vivo.';
COMMENT ON COLUMN catalog_items.created_at IS 'Data/hora de criação do registro.';
COMMENT ON COLUMN catalog_items.created_by IS 'Usuário que criou o registro.';
COMMENT ON COLUMN catalog_items.updated_at IS 'Data/hora da última alteração.';
COMMENT ON COLUMN catalog_items.updated_by IS 'Usuário que fez a última alteração.';

-- Índices: só o que realmente consultamos (listagem/filtros/busca). SEM GIN no
-- extra_data por enquanto — a listagem paginada nunca lê essa coluna.
CREATE INDEX idx_item_main  ON catalog_items(tenant_id, status, category_id);
CREATE INDEX idx_item_alive ON catalog_items(tenant_id, name) WHERE deleted_at IS NULL;
-- Vetorial p/ IA. Em produção com base grande, recriar com CONCURRENTLY fora de transação.
CREATE INDEX idx_item_embed ON catalog_items USING hnsw (embedding vector_cosine_ops);

-- ============================================================================
-- 4) VARIANTES — versões comerciais do item (grade)
-- ============================================================================
CREATE TABLE catalog_item_variants (
    id               UUID          PRIMARY KEY,
    tenant_id        UUID          NOT NULL REFERENCES tenants(id),
    item_id          UUID          NOT NULL REFERENCES catalog_items(id) ON DELETE CASCADE,
    sku              VARCHAR(40)   NOT NULL,
    ean              VARCHAR(14),
    extra_data       JSONB         NOT NULL DEFAULT '{}',
    price_adjustment NUMERIC(15,2) NOT NULL DEFAULT 0,
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by       UUID,
    updated_at       TIMESTAMPTZ,
    updated_by       UUID,
    CONSTRAINT uq_variant_sku UNIQUE (tenant_id, sku)
);

COMMENT ON TABLE  catalog_item_variants IS 'Versões vendáveis de um item com grade (ex.: camiseta M/Azul). Item sem grade = zero linhas aqui.';
COMMENT ON COLUMN catalog_item_variants.id IS 'Identificador único da variante.';
COMMENT ON COLUMN catalog_item_variants.tenant_id IS 'Tenant dono do registro (isolamento RLS).';
COMMENT ON COLUMN catalog_item_variants.item_id IS 'Item pai ao qual a variante pertence.';
COMMENT ON COLUMN catalog_item_variants.sku IS 'SKU próprio da variante (único por tenant). É o que sai na nota/etiqueta.';
COMMENT ON COLUMN catalog_item_variants.ean IS 'Código de barras específico da variante (opcional).';
COMMENT ON COLUMN catalog_item_variants.extra_data IS 'Combinação da grade em JSONB. Ex.: {"size":"M","color":"Azul"}.';
COMMENT ON COLUMN catalog_item_variants.price_adjustment IS 'Acréscimo/desconto sobre o preço do item pai (ex.: +5,00).';
COMMENT ON COLUMN catalog_item_variants.active IS 'Variante ativa para venda?';
COMMENT ON COLUMN catalog_item_variants.created_at IS 'Data/hora de criação do registro.';
COMMENT ON COLUMN catalog_item_variants.created_by IS 'Usuário que criou o registro.';
COMMENT ON COLUMN catalog_item_variants.updated_at IS 'Data/hora da última alteração.';
COMMENT ON COLUMN catalog_item_variants.updated_by IS 'Usuário que fez a última alteração.';

CREATE INDEX idx_variant_item ON catalog_item_variants(item_id, active);

-- ============================================================================
-- 5) RLS — isolamento por tenant (mesmo padrão dos módulos anteriores)
-- ============================================================================
ALTER TABLE catalog_categories     ENABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_attribute_defs ENABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_items          ENABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_item_variants  ENABLE ROW LEVEL SECURITY;

CREATE POLICY rls_catalog_categories ON catalog_categories
    USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY rls_catalog_attribute_defs ON catalog_attribute_defs
    USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY rls_catalog_items ON catalog_items
    USING (tenant_id = current_setting('app.current_tenant')::uuid);
CREATE POLICY rls_catalog_item_variants ON catalog_item_variants
    USING (tenant_id = current_setting('app.current_tenant')::uuid);

-- ============================================================================
-- 6) SEEDS — permissões do módulo + vínculo às roles soberanas
--    (insert à prova de constraints: funciona com ou sem unique em name)
-- ============================================================================
SET app.current_tenant = '00000000-0000-0000-0000-000000000000';

INSERT INTO permissions (id, name, description, category, created_at)
SELECT gen_random_uuid(), v.name, v.description, v.category, now()
FROM (VALUES
    ('catalog:read',   'Consultar produtos e serviços do catálogo', 'CATALOG'),
    ('catalog:write',  'Criar e editar produtos e serviços',        'CATALOG'),
    ('catalog:delete', 'Excluir produtos e serviços',               'CATALOG')
) AS v(name, description, category)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.name = v.name);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name IN ('ROLE_SYSTEM_ADMIN', 'ROLE_ADMIN')
  AND p.name LIKE 'catalog:%'
ON CONFLICT (role_id, permission_id) DO NOTHING;

RESET app.current_tenant;

-- ============================================================================
-- EXEMPLO OPCIONAL (descomente para testar o fluxo de atributos):
-- seção "Medicamentos" com 2 campos, sendo 1 obrigatório
-- ============================================================================
-- INSERT INTO catalog_categories (id, tenant_id, name, vertical)
-- VALUES (gen_random_uuid(), '00000000-0000-0000-0000-000000000000', 'Medicamentos', 'SANITARY');
--
-- INSERT INTO catalog_attribute_defs (id, tenant_id, category_id, key, label, data_type, required, sort_order)
-- VALUES
--  (gen_random_uuid(), '00000000-0000-0000-0000-000000000000', '<id_da_categoria>', 'registry_number',   'Registro ANVISA', 'TEXT', TRUE,  1),
--  (gen_random_uuid(), '00000000-0000-0000-0000-000000000000', '<id_da_categoria>', 'active_ingredient', 'Princípio Ativo', 'TEXT', FALSE, 2);