-- ============================================================================
-- SEED DE DEMONSTRAÇÃO — smart-catalog (somente tenant principal)
-- ----------------------------------------------------------------------------
-- Cria: 17 categorias (árvore) · 14 campos extras (defs) · 16 itens
--         (13 produtos + 3 serviços) · 4 variantes · statuses variados
-- Idempotente: se já existir a seção "Eletrônicos" no tenant, não faz nada.
-- ============================================================================

DO $$
DECLARE
    v_tenant UUID;
    -- Eletrônicos
    v_ele UUID; v_per UUID; v_tec UUID; v_mou UUID; v_aud UUID;
    -- Vestuário
    v_ves UUID; v_cam UUID;
    -- Medicamentos
    v_med UUID; v_ana UUID;
    -- Químicos
    v_qui UUID; v_des UUID;
    -- Construção
    v_con UUID; v_cim UUID;
    -- Serviços
    v_srv UUID; v_srv_ti UUID; v_srv_man UUID;
    -- Itens
    v_item_camiseta UUID;
BEGIN
    -- ── Resolve o tenant principal (primeiro tenant não-sistema) ──────────
    PERFORM set_config('app.current_tenant',
                       '00000000-0000-0000-0000-000000000000', true);
    SELECT id INTO v_tenant
    FROM tenants
    WHERE id <> '00000000-0000-0000-0000-000000000000'
    ORDER BY created_at
    LIMIT 1;

    IF v_tenant IS NULL THEN
        RAISE EXCEPTION 'Seed catalog: tenant principal não encontrado. Verifique a tabela tenants.';
    END IF;

    -- A partir daqui, todas as operações acontecem DENTRO do tenant (RLS)
    PERFORM set_config('app.current_tenant', v_tenant::text, true);

    -- ── Idempotência ───────────────────────────────────────────────────────
    IF EXISTS (SELECT 1 FROM catalog_categories
               WHERE tenant_id = v_tenant AND name = 'Eletrônicos') THEN
        RAISE NOTICE 'Seed do catálogo já aplicado para este tenant. Nada a fazer.';
        RETURN;
    END IF;

    -- ========================================================================
    -- 1) CATEGORIAS (árvore + família de negócio)
    -- ========================================================================
    v_ele := gen_random_uuid(); v_per := gen_random_uuid();
    v_tec := gen_random_uuid(); v_mou := gen_random_uuid(); v_aud := gen_random_uuid();
    v_ves := gen_random_uuid(); v_cam := gen_random_uuid();
    v_med := gen_random_uuid(); v_ana := gen_random_uuid();
    v_qui := gen_random_uuid(); v_des := gen_random_uuid();
    v_con := gen_random_uuid(); v_cim := gen_random_uuid();
    v_srv := gen_random_uuid(); v_srv_ti := gen_random_uuid(); v_srv_man := gen_random_uuid();

    INSERT INTO catalog_categories
        (id, tenant_id, parent_id, name, vertical, active) VALUES
    -- Eletrônicos e periféricos
    (v_ele, v_tenant, NULL,  'Eletrônicos',              'GENERIC',      true),
    (v_per, v_tenant, v_ele, 'Periféricos',              'GENERIC',      true),
    (v_tec, v_tenant, v_per, 'Teclados',                 'GENERIC',      true),
    (v_mou, v_tenant, v_per, 'Mouses',                   'GENERIC',      true),
    (v_aud, v_tenant, v_ele, 'Áudio',                    'GENERIC',      true),
    -- Vestuário (grade tamanho/cor)
    (v_ves, v_tenant, NULL,  'Vestuário',                'APPAREL',      true),
    (v_cam, v_tenant, v_ves, 'Camisetas',                'APPAREL',      true),
    -- Medicamentos (família sanitária)
    (v_med, v_tenant, NULL,  'Medicamentos',             'SANITARY',     true),
    (v_ana, v_tenant, v_med, 'Analgésicos',              'SANITARY',     true),
    -- Químicos e saneantes (perigosos)
    (v_qui, v_tenant, NULL,  'Químicos e Saneantes',     'HAZARDOUS',    true),
    (v_des, v_tenant, v_qui, 'Desinfetantes',            'HAZARDOUS',    true),
    -- Construção
    (v_con, v_tenant, NULL,  'Construção',               'CONSTRUCTION', true),
    (v_cim, v_tenant, v_con, 'Cimentos e Argamassas',    'CONSTRUCTION', true),
    -- Serviços
    (v_srv,     v_tenant, NULL,    'Serviços',                  'SERVICE', true),
    (v_srv_ti,  v_tenant, v_srv,   'Serviços de TI',            'SERVICE', true),
    (v_srv_man, v_tenant, v_srv,   'Serviços de Manutenção',    'SERVICE', true);

    -- ========================================================================
    -- 2) CAMPOS EXTRAS POR CATEGORIA (o "formulário em branco")
    -- ========================================================================
    INSERT INTO catalog_attribute_defs
        (id, tenant_id, category_id, key, label, data_type, unit, options, required, applies_to, sort_order) VALUES
    -- Teclados
    (gen_random_uuid(), v_tenant, v_tec, 'layout',     'Layout das teclas', 'ENUM',    NULL, '["ABNT2","ANSI"]', true,  'PRODUCT', 1),
    (gen_random_uuid(), v_tenant, v_tec, 'mechanical', 'Mecânico?',         'BOOLEAN', NULL, NULL,               false, 'PRODUCT', 2),
    -- Camisetas
    (gen_random_uuid(), v_tenant, v_cam, 'composition', 'Composição do tecido', 'TEXT', NULL, NULL, true,  'PRODUCT', 1),
    (gen_random_uuid(), v_tenant, v_cam, 'gender',      'Gênero',             'ENUM', NULL, '["Masculino","Feminino","Unisex"]', true, 'PRODUCT', 2),
    -- Analgésicos (sanitário)
    (gen_random_uuid(), v_tenant, v_ana, 'registry_number',   'Registro ANVISA',  'TEXT',    NULL, NULL, true,  'PRODUCT', 1),
    (gen_random_uuid(), v_tenant, v_ana, 'active_ingredient', 'Princípio Ativo',  'TEXT',    NULL, NULL, false, 'PRODUCT', 2),
    (gen_random_uuid(), v_tenant, v_ana, 'controlled',        'Controlado?',      'BOOLEAN', NULL, NULL, false, 'PRODUCT', 3),
    -- Desinfetantes (perigosos)
    (gen_random_uuid(), v_tenant, v_des, 'registry_number', 'Registro ANVISA (saneante)', 'TEXT', NULL, NULL, true,  'PRODUCT', 1),
    (gen_random_uuid(), v_tenant, v_des, 'un_number',       'Nº ONU',                   'TEXT', NULL, NULL, false, 'PRODUCT', 2),
    (gen_random_uuid(), v_tenant, v_des, 'risk_class',      'Classe de risco',          'TEXT', NULL, NULL, false, 'PRODUCT', 3),
    -- Cimentos e argamassas
    (gen_random_uuid(), v_tenant, v_cim, 'norm',             'Norma técnica',      'TEXT',   NULL, NULL, true,  'PRODUCT', 1),
    (gen_random_uuid(), v_tenant, v_cim, 'packing',          'Embalagem',          'ENUM',   NULL, '["25 kg","50 kg"]', true, 'PRODUCT', 2),
    (gen_random_uuid(), v_tenant, v_cim, 'units_per_pallet', 'Unidades por pallet','NUMBER', 'un', NULL, false, 'PRODUCT', 3),
    -- Serviços de TI
    (gen_random_uuid(), v_tenant, v_srv_ti, 'duration_hours', 'Duração padrão', 'NUMBER', 'h', NULL, false, 'SERVICE', 1);

    -- ========================================================================
    -- 3) ITENS (produtos + serviços, com statuses e perfis variados)
    -- ========================================================================
    v_item_camiseta := gen_random_uuid();

    INSERT INTO catalog_items
        (id, tenant_id, category_id, item_type, status, sku, ean, name, description,
         unit_of_measure, sale_price, cost_price, ncm, cest, tax_origin, cst_icms, csosn,
         service_code, iss_rate, tracks_batch, tracks_expiry, is_hazardous, extra_data,
         min_stock, max_stock, reorder_stock) VALUES
    -- ── Eletrônicos ──
    (gen_random_uuid(), v_tenant, v_tec, 'PRODUCT', 'ACTIVE', 'PRD-000001', '7891234567895',
     'Teclado Mecânico TKL RGB', 'Teclado mecânico switch blue, ABNT2, retroiluminado.',
     'UN', 349.90, 210.00, '8471.60.52', NULL, '0', '00', NULL, NULL, NULL,
     false, false, false, '{"layout":"ABNT2","mechanical":true}', 5, 50, 10),
    (gen_random_uuid(), v_tenant, v_tec, 'PRODUCT', 'INACTIVE', 'PRD-000002', '7898543216780',
     'Teclado Comum USB ABNT2', 'Modelo de entrada, linha descontinuada.',
     'UN', 89.90, 45.00, '8471.60.52', NULL, '0', '00', NULL, NULL, NULL,
     false, false, false, '{"layout":"ABNT2","mechanical":false}', 0, 0, 0),
    (gen_random_uuid(), v_tenant, v_mou, 'PRODUCT', 'ACTIVE', 'PRD-000003', '7896001234567',
     'Mouse Sem Fio Silencioso 2.4GHz', 'Mouse silencioso com receptor USB.',
     'UN', 129.90, 62.00, '8471.60.54', NULL, '0', '00', NULL, NULL, NULL,
     false, false, false, '{}', 10, 80, 15),
    (gen_random_uuid(), v_tenant, v_aud, 'PRODUCT', 'ACTIVE', 'PRD-000004', '7891111222237',
     'Headset Bluetooth Pro c/ Microfone', 'Headset sem fio p/ reuniões e home office.',
     'UN', 259.90, 140.00, '8518.30.00', NULL, '0', '00', NULL, NULL, NULL,
     false, false, false, '{}', 5, 40, 8),
    -- ── Vestuário (com variantes abaixo) ──
    (v_item_camiseta, v_tenant, v_cam, 'PRODUCT', 'ACTIVE', 'PRD-000005', '7893333444459',
     'Camiseta Básica 100% Algodão', 'Camiseta unisex gola redonda, malha penteada.',
     'UN', 49.90, 18.00, '6109.10.00', NULL, '0', '00', NULL, NULL, NULL,
     false, false, false, '{"composition":"100% algodão","gender":"Unisex"}', 20, 200, 40),
    (gen_random_uuid(), v_tenant, v_cam, 'PRODUCT', 'DRAFT', 'PRD-000006', NULL,
     'Camiseta Premium Dry-Fit', 'Linha esportiva com secagem rápida (aguardando aprovação).',
     'UN', 79.90, 32.00, '6109.10.00', NULL, '0', '00', NULL, NULL, NULL,
     false, false, false, '{"composition":"100% poliéster","gender":"Unisex"}', 10, 100, 20),
    -- ── Medicamentos (lote + validade + ANVISA) ──
    (gen_random_uuid(), v_tenant, v_ana, 'PRODUCT', 'ACTIVE', 'PRD-000007', '7891000100011',
     'Dipirona Sódica 500mg cx 20 comp', 'Analgésico e antitérmico de uso adulto.',
     'CX', 12.90, 6.10, '3004.90.99', NULL, '0', '00', NULL, NULL, NULL,
     true, true, false,
     '{"registry_number":"1.0235-8","active_ingredient":"Dipirona sódica","controlled":false}',
     50, 500, 100),
    (gen_random_uuid(), v_tenant, v_ana, 'PRODUCT', 'ACTIVE', 'PRD-000008', NULL,
     'Paracetamol 750mg cx 20 comp', 'Antitérmico e analgésico.',
     'CX', 15.90, 7.20, '3004.90.99', NULL, '0', '00', NULL, NULL, NULL,
     true, true, false,
     '{"registry_number":"1.1234-5","active_ingredient":"Paracetamol","controlled":false}',
     40, 400, 80),
    (gen_random_uuid(), v_tenant, v_ana, 'PRODUCT', 'DRAFT', 'PRD-000009', NULL,
     'Ibuprofeno 400mg cx 10 comp', 'Anti-inflamatório (rascunho aguardando preço de custo).',
     'CX', 18.50, 0.00, '3004.90.99', NULL, '0', '00', NULL, NULL, NULL,
     true, true, false,
     '{"registry_number":"1.5678-2","active_ingredient":"Ibuprofeno","controlled":false}',
     30, 300, 60),
    -- ── Químicos / saneantes (perigosos) ──
    (gen_random_uuid(), v_tenant, v_des, 'PRODUCT', 'ACTIVE', 'PRD-000010', NULL,
     'Desinfetante Hospitalar 1L', 'Hipoclorito p/ superfícies hospitalares.',
     'UN', 22.90, 10.50, '3808.94.29', NULL, '0', '00', NULL, NULL, NULL,
     true, true, true,
     '{"registry_number":"3.2456-7","un_number":"UN1791","risk_class":"8"}',
     20, 200, 40),
    (gen_random_uuid(), v_tenant, v_des, 'PRODUCT', 'ACTIVE', 'PRD-000011', NULL,
     'Álcool 70% 1L', 'Álcool etílico hidratado 70% p/ desinfecção.',
     'UN', 9.90, 4.20, '2207.10.19', NULL, '0', '00', NULL, NULL, NULL,
     true, true, true,
     '{"registry_number":"3.9012-4","un_number":"UN1170","risk_class":"3"}',
     60, 600, 120),
    -- ── Construção ──
    (gen_random_uuid(), v_tenant, v_cim, 'PRODUCT', 'ACTIVE', 'PRD-000012', NULL,
     'Cimento Portland CP-II 50kg', 'Cimento composto p/ uso estrutural geral.',
     'UN', 42.90, 31.00, '2523.29.10', NULL, '0', '00', NULL, NULL, NULL,
     false, false, false,
     '{"norm":"NBR 11578","packing":"50 kg","units_per_pallet":40}',
     100, 800, 200),
    (gen_random_uuid(), v_tenant, v_cim, 'PRODUCT', 'ACTIVE', 'PRD-000013', NULL,
     'Argamassa Colante AC-III 20kg', 'Argamassa p/ cerâmicas de grande formato.',
     'UN', 34.90, 22.00, '3824.50.00', NULL, '0', '00', NULL, NULL, NULL,
     false, false, false,
     '{"norm":"NBR 14081","packing":"25 kg","units_per_pallet":60}',
     50, 400, 100),
    -- ── Serviços ──
    (gen_random_uuid(), v_tenant, v_srv_ti, 'SERVICE', 'ACTIVE', 'SRV-000001', NULL,
     'Hora Técnica de TI', 'Suporte/implantação por hora, presencial ou remoto.',
     'HR', 180.00, 90.00, NULL, NULL, NULL, NULL, NULL, '1.01', 5.00,
     false, false, false, '{"duration_hours":1}', NULL, NULL, NULL),
    (gen_random_uuid(), v_tenant, v_srv_ti, 'SERVICE', 'ACTIVE', 'SRV-000002', NULL,
     'Consultoria em Infraestrutura de TI', 'Diagnóstico e projeto de infraestrutura.',
     'HR', 350.00, 200.00, NULL, NULL, NULL, NULL, NULL, '17.10', 5.00,
     false, false, false, '{"duration_hours":4}', NULL, NULL, NULL),
    (gen_random_uuid(), v_tenant, v_srv_man, 'SERVICE', 'DRAFT', 'SRV-000003', NULL,
     'Manutenção Elétrica Residencial (visita)', 'Visita técnica com orçamento (rascunho).',
     'HR', 220.00, 120.00, NULL, NULL, NULL, NULL, NULL, '7.02', 5.00,
     false, false, false, '{"duration_hours":2}', NULL, NULL, NULL);

    -- ========================================================================
    -- 4) VARIANTES da Camiseta Básica (grade tamanho × cor)
    -- ========================================================================
    INSERT INTO catalog_item_variants
        (id, tenant_id, item_id, sku, ean, extra_data, price_adjustment, active) VALUES
    (gen_random_uuid(), v_tenant, v_item_camiseta, 'PRD-000005-M-AZ', NULL,
     '{"size":"M","color":"Azul"}',  0.00, true),
    (gen_random_uuid(), v_tenant, v_item_camiseta, 'PRD-000005-G-AZ', NULL,
     '{"size":"G","color":"Azul"}',  0.00, true),
    (gen_random_uuid(), v_tenant, v_item_camiseta, 'PRD-000005-M-PR', NULL,
     '{"size":"M","color":"Preto"}', 0.00, true),
    (gen_random_uuid(), v_tenant, v_item_camiseta, 'PRD-000005-G-PR', NULL,
     '{"size":"G","color":"Preto"}', 2.00, true);

    RAISE NOTICE 'Seed do smart-catalog aplicado com sucesso no tenant %', v_tenant;
END $$;