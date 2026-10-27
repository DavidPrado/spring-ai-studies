package com.smart_extract_service.modules.bulk.infrastructure.adapters.bulk;

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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class CatalogItemsBulkHandler implements BulkEntityHandler {

    private final CatalogItemRepositoryPort repository;

    @Override
    public String entity() {
        return "catalog-items";
    }

    @Override
    public List<String> headers() {
        // Cabeçalhos para EXPORTAÇÃO (o que o usuário vê no Excel)
        return List.of("SKU", "EAN", "Nome", "Categoria", "Tipo", "Status",
                "Unidade", "Preço de Venda", "Preço de Custo", "Margem %");
    }

    @Override
    public long count(UUID tenantId, Map<String, String> filters) {
        return repository.countByFilters(tenantId, filters);
    }

    @Override
    public void streamRows(UUID tenantId, Map<String, String> filters, Consumer<List<BulkCell>> rowSink) {
        repository.streamForExport(tenantId, filters, row -> rowSink.accept(List.of(
                BulkCell.text(row.sku()),
                BulkCell.text(row.ean() == null ? "" : row.ean()),
                BulkCell.text(row.name()),
                BulkCell.text(row.categoryName() == null ? "" : row.categoryName()),
                BulkCell.text(typeLabel(row.itemType())),
                BulkCell.text(statusLabel(row.status())),
                BulkCell.text(row.unitOfMeasure()),
                BulkCell.number(row.salePrice()),
                BulkCell.number(row.costPrice()),
                BulkCell.percent(margin(row.salePrice(), row.costPrice()))
        )));
    }

    @Override
    public List<String> validateRow(Map<String, String> row) {
        List<String> errors = new ArrayList<>();

        String sku = row.get("sku");
        String name = row.get("name");

        if (sku == null || sku.isBlank()) {
            errors.add("O campo 'sku' é obrigatório.");
        }
        if (name == null || name.isBlank()) {
            errors.add("O campo 'nome' é obrigatório.");
        }

        String salePriceStr = row.get("salePrice");
        if (salePriceStr != null && !salePriceStr.isBlank()) {
            try {
                BigDecimal price = new BigDecimal(salePriceStr.replace(',', '.'));
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    errors.add("O 'Preço de Venda' não pode ser negativo.");
                }
            } catch (NumberFormatException e) {
                errors.add("O 'Preço de Venda' deve ser um número válido.");
            }
        }

        return errors;
    }

    @Override
    public ImportResult processBatch(UUID tenantId, List<Map<String, String>> rows, String mode) {
        int successCount = 0;
        List<RowError> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            try {
                CatalogItem item = mapToDomain(tenantId, row);

                if ("INSERT_ONLY".equalsIgnoreCase(mode)) {
                    if (repository.existsSku(tenantId, item.getSku(), null)) {
                        errors.add(new RowError(i + 1, row.toString(), List.of("SKU já existe: " + item.getSku())));
                        continue;
                    }
                }

                repository.save(item);
                successCount++;

            } catch (Exception e) {
                errors.add(new RowError(i + 1, row.toString(), List.of("Erro ao processar: " + e.getMessage())));
            }
        }

        return new ImportResult(rows.size(), successCount, errors.size(), errors);
    }

    @Override
    public List<Map<String, String>> generateTemplate() {
        List<Map<String, String>> template = new ArrayList<>();

        // ★ CORREÇÃO 1: As chaves do cabeçalho DEVEM ser exatamente os nomes
        // que o parser CSV vai usar para montar o Map (minúsculas, sem espaços).
        Map<String, String> header = new LinkedHashMap<>();
        header.put("sku", "sku");
        header.put("ean", "ean");
        header.put("name", "name");
        header.put("categoryName", "categoryName");
        header.put("itemType", "itemType");
        header.put("status", "status");
        header.put("unitOfMeasure", "unitOfMeasure");
        header.put("salePrice", "salePrice");
        header.put("costPrice", "costPrice");
        template.add(header);

        // Linha 2: Exemplo de dados (aqui sim podemos colocar valores descritivos)
        Map<String, String> example = new LinkedHashMap<>();
        example.put("sku", "PRD-00001");
        example.put("ean", "7891000100103");
        example.put("name", "Café Bourbon Especial");
        example.put("categoryName", "Bebidas");
        example.put("itemType", "PRODUCT");
        example.put("status", "ACTIVE");
        example.put("unitOfMeasure", "UN");
        example.put("salePrice", "89.90");
        example.put("costPrice", "52.00");
        template.add(example);

        return template;
    }

    private CatalogItem mapToDomain(UUID tenantId, Map<String, String> row) {
        CatalogItem item = new CatalogItem();

        // ★ CORREÇÃO 2: Se o seu modelo de domínio exige tenantId para RLS, descomente a linha abaixo:
        // item.setTenantId(tenantId);

        item.setSku(row.get("sku") != null ? row.get("sku").trim() : "");
        item.setEan(row.get("ean") != null ? row.get("ean").trim() : "");
        item.setName(row.get("name") != null ? row.get("name").trim() : "");

        String typeStr = row.get("itemType");
        item.setItemType("SERVICE".equalsIgnoreCase(typeStr) ? ItemType.SERVICE : ItemType.PRODUCT);

        String statusStr = row.get("status");
        if ("ACTIVE".equalsIgnoreCase(statusStr)) item.setStatus(ItemStatus.ACTIVE);
        else if ("INACTIVE".equalsIgnoreCase(statusStr)) item.setStatus(ItemStatus.INACTIVE);
        else item.setStatus(ItemStatus.DRAFT);

        // ★ CORREÇÃO 3: Proteção contra NullPointerException ou IllegalArgumentException
        // no Enum.valueOf se o usuário digitar algo errado ou deixar em branco no CSV.
        String unitStr = row.get("unitOfMeasure");
        try {
            item.setUnitOfMeasure(UnitOfMeasure.valueOf(
                    unitStr != null && !unitStr.isBlank() ? unitStr.trim().toUpperCase() : "UN"
            ));
        } catch (IllegalArgumentException | NullPointerException e) {
            item.setUnitOfMeasure(UnitOfMeasure.UN); // Fallback seguro
        }

        String salePriceStr = row.get("salePrice");
        if (salePriceStr != null && !salePriceStr.isBlank()) {
            item.setSalePrice(new BigDecimal(salePriceStr.replace(',', '.')));
        } else {
            item.setSalePrice(BigDecimal.ZERO);
        }

        String costPriceStr = row.get("costPrice");
        if (costPriceStr != null && !costPriceStr.isBlank()) {
            item.setCostPrice(new BigDecimal(costPriceStr.replace(',', '.')));
        } else {
            item.setCostPrice(BigDecimal.ZERO);
        }

        return item;
    }

    private String typeLabel(String type) {
        return "SERVICE".equals(type) ? "Serviço" : "Produto";
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "ACTIVE" -> "Ativo";
            case "INACTIVE" -> "Inativo";
            default -> "Rascunho";
        };
    }

    private BigDecimal margin(BigDecimal sale, BigDecimal cost) {
        if (sale == null || sale.signum() <= 0) return BigDecimal.ZERO;
        BigDecimal c = cost == null ? BigDecimal.ZERO : cost;
        return sale.subtract(c)
                .divide(sale, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}