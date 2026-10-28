package com.smart_extract_service.modules.bulk.infrastructure.adapters.bulk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart_extract_service.modules.bulk.application.dto.ImportResult;
import com.smart_extract_service.modules.bulk.application.dto.RowError;
import com.smart_extract_service.modules.bulk.application.spi.BulkCell;
import com.smart_extract_service.modules.bulk.application.spi.BulkEntityHandler;
import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import com.smart_extract_service.modules.catalog.domain.enums.ItemType;
import com.smart_extract_service.modules.catalog.domain.enums.UnitOfMeasure;
import com.smart_extract_service.modules.catalog.domain.model.CatalogItem;
import com.smart_extract_service.modules.catalog.domain.ports.output.CatalogItemRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

/**
 * ============================================================================
 * CLASSE: CatalogItemsBulkHandler
 * ============================================================================
 *
 * PROPÓSITO NO SISTEMA:
 * Esta classe é o "Adaptador de Infraestrutura" responsável por processar
 * importações e exportações em massa especificamente para a entidade CatalogItem.
 *
 * POR QUE ELA EXISTE?
 * 1. Desacoplamento: Separa a lógica complexa de parsing de CSV/JSON da regra
 *    de negócio pura, mantendo o domínio limpo.
 * 2. Resiliência: Processa em lotes (batches), validando linha a linha. Se uma
 *    linha tiver erro, ela é registrada, mas não interrompe as demais.
 * 3. Padronização: Implementa a interface BulkEntityHandler, permitindo que o
 *    sistema adicione novos importadores (ex: Clients, Suppliers) sem mudar o core.
 *
 * FLUXO DE EXECUÇÃO:
 * Upload CSV -> Controller cria Job QUEUED -> Worker pega o Job ->
 * Worker chama este Handler -> Validação -> Mapeamento para Domínio -> Save no Banco.
 * ============================================================================
 */
@Slf4j
@Component("catalog-items") // ⚠️ CRÍTICO: O nome do bean DEVE ser "catalog-items" para o Map do Spring encontrar
@RequiredArgsConstructor
public class CatalogItemsBulkHandler implements BulkEntityHandler {

    // Dependências injetadas automaticamente pelo Spring
    private final CatalogItemRepositoryPort repository;
    private final ObjectMapper objectMapper; // Usado para parsear o campo extraData (JSON)

    /**
     * 1. IDENTIFICADOR DA ENTIDADE
     * Retorna a chave única usada pelo BulkJobWorker para encontrar este handler.
     */
    @Override
    public String entity() {
        return "catalog-items";
    }

    /**
     * 2. CABEÇALHOS PARA EXPORTAÇÃO/TEMPLATE
     * Define a ordem e os nomes exatos das colunas que aparecerão no CSV de exportação
     * ou no modelo de importação baixado pelo usuário.
     */
    @Override
    public List<String> headers() {
        return List.of(
                "sku", "name", "description", "categoryId", "itemType", "status", "unitOfMeasure",
                "salePrice", "costPrice", "ncm", "cest", "taxOrigin", "cstIcms", "csosn",
                "serviceCode", "issRate", "tracksBatch", "tracksExpiry", "isHazardous",
                "minStock", "maxStock", "reorderStock", "extraData"
        );
    }

    /**
     * 3. CONTAGEM DE REGISTROS
     * Usado pela UI de exportação para mostrar progresso ("Exportando 50 de 1000 itens...").
     */
    @Override
    public long count(UUID tenantId, Map<String, String> filters) {
        return repository.countByFilters(tenantId, filters);
    }

    /**
     * 4. STREAMING DE DADOS (EXPORTAÇÃO)
     * Lê os dados do banco em chunks (lotes) para não estourar a memória (OutOfMemoryError)
     * em bases de dados grandes, convertendo cada linha para o formato de células formatadas.
     */
    @Override
    public void streamRows(UUID tenantId, Map<String, String> filters, Consumer<List<BulkCell>> rowSink) {
        repository.streamForExport(tenantId, filters, row -> {

            // Mapeia cada campo do objeto de retorno do repositório para uma BulkCell formatada.
            // Tratamento de null é aplicado para evitar NullPointerException durante a exportação.
            List<BulkCell> cells = List.of(
                    BulkCell.text(row.sku() != null ? row.sku() : ""),
                    BulkCell.text(row.name() != null ? row.name() : ""),
                    BulkCell.text(row.description() != null ? row.description() : ""),
                    BulkCell.text(row.categoryId() != null ? row.categoryId().toString() : ""),
                    BulkCell.text(row.itemType() != null ? row.itemType().name() : ""),
                    BulkCell.text(row.status() != null ? row.status().name() : ""),
                    BulkCell.text(row.unitOfMeasure() != null ? row.unitOfMeasure().name() : ""),
                    BulkCell.number(row.salePrice()),
                    BulkCell.number(row.costPrice()),

                    // Campos Fiscais
                    BulkCell.text(row.ncm() != null ? row.ncm() : ""),
                    BulkCell.text(row.cest() != null ? row.cest() : ""),
                    BulkCell.text(row.taxOrigin() != null ? row.taxOrigin() : ""),
                    BulkCell.text(row.cstIcms() != null ? row.cstIcms() : ""),
                    BulkCell.text(row.csosn() != null ? row.csosn() : ""),
                    BulkCell.text(row.serviceCode() != null ? row.serviceCode() : ""),
                    BulkCell.number(row.issRate()),

                    // Controles Verticais
                    BulkCell.bool(row.tracksBatch()),
                    BulkCell.bool(row.tracksExpiry()),
                    BulkCell.bool(row.isHazardous()), // ⚠️ Verifique se no seu record é isHazardous() ou hazardous()

                    // Estoque (Convertendo Integer para BigDecimal para o BulkCell.number)
                    BulkCell.number(row.minStock() != null ? BigDecimal.valueOf(row.minStock()) : null),
                    BulkCell.number(row.maxStock() != null ? BigDecimal.valueOf(row.maxStock()) : null),
                    BulkCell.number(row.reorderStock() != null ? BigDecimal.valueOf(row.reorderStock()) : null),

                    // Dados Extras (JSON)
                    BulkCell.text(row.extraData() != null ? row.extraData().toString() : "")
            );

            // Envia a linha formatada para o renderer (CSV ou Excel)
            rowSink.accept(cells);
        });
    }

    /**
     * 5. VALIDAÇÃO DE LINHA (PRÉ-PROCESSAMENTO)
     * Verifica se os dados brutos do CSV fazem sentido ANTES de tentar converter para o Domínio.
     * Isso evita exceções desnecessárias do Hibernate e gera relatórios de erro claros para o usuário.
     */
    @Override
    public List<String> validateRow(Map<String, String> row) {
        List<String> errors = new ArrayList<>();

        String sku = getSafeString(row, "sku");
        String name = getSafeString(row, "name");
        String categoryIdStr = getSafeString(row, "categoryId");

        // Validações de campos obrigatórios
        if (sku.isBlank()) errors.add("O campo 'sku' é obrigatório.");
        if (name.isBlank()) errors.add("O campo 'name' é obrigatório.");

        if (categoryIdStr.isBlank()) {
            errors.add("O campo 'categoryId' é obrigatório.");
        } else {
            // Valida se o formato do UUID da categoria é válido
            try {
                UUID.fromString(categoryIdStr);
            } catch (IllegalArgumentException e) {
                errors.add("O 'categoryId' deve ser um UUID válido (ex: 123e4567-e89b-12d3-a456-426614174000).");
            }
        }

        // Validação de Enum
        String itemTypeStr = getSafeString(row, "itemType");
        if (!itemTypeStr.equalsIgnoreCase("PRODUCT") && !itemTypeStr.equalsIgnoreCase("SERVICE")) {
            errors.add("O 'itemType' deve ser 'PRODUCT' ou 'SERVICE'.");
        }

        // Validações numéricas seguras
        validateDecimal(row, "salePrice", errors, "Preço de Venda");
        validateDecimal(row, "costPrice", errors, "Preço de Custo");
        validateDecimal(row, "issRate", errors, "Alíquota ISS");

        return errors;
    }

    /**
     * 6. PROCESSAMENTO EM LOTE (O CORAÇÃO DA IMPORTAÇÃO)
     * Recebe um lote de linhas JÁ VALIDADAS, converte para o modelo de domínio e salva no banco.
     * O try-catch interno garante que um erro em uma linha não pare o processamento das demais.
     */
    @Override
    public ImportResult processBatch(UUID tenantId, List<Map<String, String>> rows, String mode) {
        int successCount = 0;
        List<RowError> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            try {
                // 1. Converte Map (CSV) -> Objeto de Domínio
                CatalogItem item = mapToDomain(tenantId, row);

                // 2. Regra de Negócio: Se for INSERT_ONLY, verifica duplicidade de SKU
                if ("INSERT_ONLY".equalsIgnoreCase(mode)) {
                    if (repository.existsSku(tenantId, item.getSku(), null)) {
                        errors.add(new RowError(i + 2, row.toString(), List.of("SKU já existe no sistema: " + item.getSku())));
                        continue; // Pula para a próxima linha
                    }
                }

                // 3. Persiste no banco de dados
                repository.save(item);
                successCount++;

            } catch (Exception e) {
                // Captura qualquer erro inesperado (ex: falha de conexão, regra de domínio) e registra
                log.error("Erro ao processar linha {} do lote para SKU {}", i + 2, row.get("sku"), e);
                errors.add(new RowError(i + 2, row.toString(), List.of("Erro ao processar: " + e.getMessage())));
            }
        }

        // Retorna o resumo do processamento deste lote
        return new ImportResult(rows.size(), successCount, errors.size(), errors);
    }

    /**
     * 7. GERAÇÃO DE TEMPLATE
     * Cria um CSV de exemplo com cabeçalhos e uma linha de dados fictícios para
     * guiar o usuário no preenchimento correto.
     */
    @Override
    public List<Map<String, String>> generateTemplate() {
        List<Map<String, String>> template = new ArrayList<>();

        // Linha 1: Cabeçalhos exatos (chaves do Map)
        Map<String, String> header = new LinkedHashMap<>();
        for (String h : headers()) {
            header.put(h, h);
        }
        template.add(header);

        // Linha 2: Exemplo prático de preenchimento
        Map<String, String> example = new LinkedHashMap<>();
        example.put("sku", "PRD-EXEMPLO-001");
        example.put("name", "Notebook Gamer Exemplo");
        example.put("description", "Notebook com RTX 4060 e 16GB RAM");
        example.put("categoryId", "3d134ae0-3998-41b0-8f6e-26e5cf2d264d"); // ⚠️ Substitua por um ID real do seu banco
        example.put("itemType", "PRODUCT");
        example.put("status", "DRAFT");
        example.put("unitOfMeasure", "UN");
        example.put("salePrice", "5000,00");
        example.put("costPrice", "3000,00");
        example.put("ncm", "8471.30.12");
        example.put("cest", "");
        example.put("taxOrigin", "0");
        example.put("cstIcms", "00");
        example.put("csosn", "102");
        example.put("serviceCode", "");
        example.put("issRate", "");
        example.put("tracksBatch", "false");
        example.put("tracksExpiry", "false");
        example.put("isHazardous", "false");
        example.put("minStock", "5");
        example.put("maxStock", "50");
        example.put("reorderStock", "10");
        example.put("extraData", "{\"cor\": \"preto\", \"garantia_meses\": 12}");

        template.add(example);
        return template;
    }

    // ========================================================================
    // MAPEAMENTO DOMÍNIO (A PEÇA-CHAVE DA CORREÇÃO)
    // ========================================================================
    /**
     * Converte uma linha bruta do CSV (Map<String, String>) em um objeto CatalogItem válido.
     * É aqui que garantimos que todas as regras de integridade do banco e do domínio sejam atendidas.
     */
    private CatalogItem mapToDomain(UUID tenantId, Map<String, String> row) {
        CatalogItem item = new CatalogItem();

        // ✅ Geração manual do ID (Obrigatório)
        item.setId(UUID.randomUUID());

        // ✅ Definição do Tenant (Obrigatório para RLS)
        item.setTenantId(tenantId);

        // ✅ Categoria com fallback seguro
        String categoryIdStr = getSafeString(row, "categoryId");
        if (!categoryIdStr.isBlank()) {
            try {
                item.setCategoryId(UUID.fromString(categoryIdStr));
            } catch (IllegalArgumentException e) {
                item.setCategoryId(getDefaultCategoryId());
            }
        } else {
            item.setCategoryId(getDefaultCategoryId());
        }

        // Identidade Básica
        item.setSku(getSafeString(row, "sku").trim());
        item.setName(getSafeString(row, "name").trim());

        // ✅ CORREÇÃO: Todos os campos de texto opcionais devem ser null se vazios
        item.setDescription(toNullIfBlank(getSafeString(row, "description")));
        item.setEan(toNullIfBlank(getSafeString(row, "ean")));

        // Enums com tratamento de erro
        String typeStr = getSafeString(row, "itemType");
        item.setItemType("SERVICE".equalsIgnoreCase(typeStr) ? ItemType.SERVICE : ItemType.PRODUCT);

        String statusStr = getSafeString(row, "status");
        if ("ACTIVE".equalsIgnoreCase(statusStr)) item.setStatus(ItemStatus.ACTIVE);
        else if ("INACTIVE".equalsIgnoreCase(statusStr)) item.setStatus(ItemStatus.INACTIVE);
        else item.setStatus(ItemStatus.DRAFT);

        String unitStr = getSafeString(row, "unitOfMeasure");
        try {
            item.setUnitOfMeasure(UnitOfMeasure.valueOf(unitStr.isBlank() ? "UN" : unitStr.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            item.setUnitOfMeasure(UnitOfMeasure.UN);
        }

        // Comercial
        item.setSalePrice(parseDecimal(getSafeString(row, "salePrice"), BigDecimal.ZERO));
        item.setCostPrice(parseDecimal(getSafeString(row, "costPrice"), BigDecimal.ZERO));

        // ✅ CORREÇÃO: Campos Fiscais Mercadoria (null se vazio)
        item.setNcm(toNullIfBlank(getSafeString(row, "ncm")));
        item.setCest(toNullIfBlank(getSafeString(row, "cest")));
        item.setTaxOrigin(toNullIfBlank(getSafeString(row, "taxOrigin")));
        item.setCstIcms(toNullIfBlank(getSafeString(row, "cstIcms")));
        item.setCsosn(toNullIfBlank(getSafeString(row, "csosn")));

        // ✅ CORREÇÃO: Campos Fiscais Serviço (null se vazio)
        item.setServiceCode(toNullIfBlank(getSafeString(row, "serviceCode")));
        item.setIssRate(parseDecimal(getSafeString(row, "issRate"), null));

        // Controles Verticais
        item.setTracksBatch(parseBoolean(getSafeString(row, "tracksBatch")));
        item.setTracksExpiry(parseBoolean(getSafeString(row, "tracksExpiry")));
        item.setHazardous(parseBoolean(getSafeString(row, "isHazardous")));

        // Estoque (null se vazio, será limpo automaticamente se for SERVICE)
        item.setMinStock(parseInteger(getSafeString(row, "minStock")));
        item.setMaxStock(parseInteger(getSafeString(row, "maxStock")));
        item.setReorderStock(parseInteger(getSafeString(row, "reorderStock")));

        // Extra Data (JSON) - parse seguro
        String extraDataStr = getSafeString(row, "extraData");
        if (!extraDataStr.isBlank()) {
            try {
                Map<String, Object> extraData = objectMapper.readValue(extraDataStr, new TypeReference<Map<String, Object>>() {});
                item.setExtraData(extraData);
            } catch (Exception e) {
                log.warn("Falha ao parsear extraData para SKU {}. Usando mapa vazio.", item.getSku());
                item.setExtraData(new HashMap<>());
            }
        }

        // Aplicar Regras de Negócio do Domínio
        item.ensureTypeConsistency();

        return item;
    }

    /**
     * ✅ MÉTODO AUXILIAR CRÍTICO:
     * Converte strings vazias ou em branco para null.
     * Isso evita que o banco de dados fique cheio de '' (strings vazias)
     * em vez de NULL (ausência de valor).
     */
    private String toNullIfBlank(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    // ========================================================================
    // MÉTODOS AUXILIARES DE PARSING SEGURO (Evitam NullPointerException)
    // ========================================================================

    private String getSafeString(Map<String, String> row, String key) {
        return row.getOrDefault(key, "").trim();
    }

    private BigDecimal parseDecimal(String value, BigDecimal defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            // Substitui vírgula por ponto para compatibilidade com BigDecimal
            return new BigDecimal(value.replace(',', '.').trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.replace(',', '.').trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "sim".equalsIgnoreCase(value) || "1".equals(value);
    }

    private void validateDecimal(Map<String, String> row, String key, List<String> errors, String fieldName) {
        String value = getSafeString(row, key);
        if (!value.isBlank()) {
            try {
                BigDecimal decimal = new BigDecimal(value.replace(',', '.'));
                if (decimal.compareTo(BigDecimal.ZERO) < 0) {
                    errors.add("O '" + fieldName + "' não pode ser negativo.");
                }
            } catch (NumberFormatException e) {
                errors.add("O '" + fieldName + "' deve ser um número válido (ex: 100,50 ou 100.50).");
            }
        }
    }

    /**
     * Retorna um UUID de categoria padrão caso o CSV venha sem essa informação.
     * ⚠️ IMPORTANTE: Substitua este UUID por um que REALMENTE exista na sua tabela catalog_categories.
     */
    private UUID getDefaultCategoryId() {
        return UUID.fromString("3d134ae0-3998-41b0-8f6e-26e5cf2d264d");
    }
}