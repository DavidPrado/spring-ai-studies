SELECT 'categorias' AS tabela, tenant_id, count(*) AS registros
FROM catalog_categories GROUP BY tenant_id
UNION ALL
SELECT 'campos extras', tenant_id, count(*) FROM catalog_attribute_defs GROUP BY tenant_id
UNION ALL
SELECT 'itens',         tenant_id, count(*) FROM catalog_items          GROUP BY tenant_id
UNION ALL
SELECT 'variantes',     tenant_id, count(*) FROM catalog_item_variants  GROUP BY tenant_id
ORDER BY 1, 2;

SELECT 'SKU item' AS conflito, s.sku
FROM catalog_items s
JOIN catalog_items t ON t.tenant_id = '00000000-0000-0000-0000-000000000000' AND t.sku = s.sku
WHERE s.tenant_id = '296cad03-096a-424f-a4da-52278f5ee0f1'
UNION ALL
SELECT 'EAN item', s.ean
FROM catalog_items s
JOIN catalog_items t ON t.tenant_id = '00000000-0000-0000-0000-000000000000' AND t.ean = s.ean
WHERE s.tenant_id = '296cad03-096a-424f-a4da-52278f5ee0f1' AND s.ean IS NOT NULL
UNION ALL
SELECT 'SKU variante', s.sku
FROM catalog_item_variants s
JOIN catalog_item_variants t ON t.tenant_id = '00000000-0000-0000-0000-000000000000' AND t.sku = s.sku
WHERE s.tenant_id = '296cad03-096a-424f-a4da-52278f5ee0f1';


BEGIN;

ALTER TABLE catalog_categories     DISABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_attribute_defs DISABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_items          DISABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_item_variants  DISABLE ROW LEVEL SECURITY;

UPDATE catalog_categories
SET tenant_id = '00000000-0000-0000-0000-000000000000'
WHERE tenant_id = '296cad03-096a-424f-a4da-52278f5ee0f1';

UPDATE catalog_attribute_defs
SET tenant_id = '00000000-0000-0000-0000-000000000000'
WHERE tenant_id = '296cad03-096a-424f-a4da-52278f5ee0f1';

UPDATE catalog_items
SET tenant_id = '00000000-0000-0000-0000-000000000000'
WHERE tenant_id = '296cad03-096a-424f-a4da-52278f5ee0f1';

UPDATE catalog_item_variants
SET tenant_id = '00000000-0000-0000-0000-000000000000'
WHERE tenant_id = '296cad03-096a-424f-a4da-52278f5ee0f1';

ALTER TABLE catalog_categories     ENABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_attribute_defs ENABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_items          ENABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_item_variants  ENABLE ROW LEVEL SECURITY;

COMMIT;


SELECT 'categorias' AS tabela, tenant_id, count(*) FROM catalog_categories GROUP BY tenant_id
UNION ALL
SELECT 'campos extras', tenant_id, count(*) FROM catalog_attribute_defs GROUP BY tenant_id
UNION ALL
SELECT 'itens',         tenant_id, count(*) FROM catalog_items          GROUP BY tenant_id
UNION ALL
SELECT 'variantes',     tenant_id, count(*) FROM catalog_item_variants  GROUP BY tenant_id
ORDER BY 1, 2;


SELECT relname, relrowsecurity
FROM pg_class
WHERE relname LIKE 'catalog_%' AND relkind = 'r';


SET app.current_tenant = '00000000-0000-0000-0000-000000000000';
SELECT sku, name, status FROM catalog_items ORDER BY sku;
RESET app.current_tenant;