package com.smart_extract_service.modules.catalog.application.usecases;

import com.smart_extract_service.core.context.Auditable;
import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.modules.catalog.application.dto.*;
import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import com.smart_extract_service.modules.catalog.domain.model.AttributeDef;
import com.smart_extract_service.modules.catalog.domain.model.CatalogCategory;
import com.smart_extract_service.modules.catalog.domain.model.CatalogItem;
import com.smart_extract_service.modules.catalog.domain.model.CatalogItemVariant;
import com.smart_extract_service.modules.catalog.domain.ports.input.CatalogItemUseCase;
import com.smart_extract_service.modules.catalog.domain.ports.output.AttributeDefRepositoryPort;
import com.smart_extract_service.modules.catalog.domain.ports.output.CatalogCategoryRepositoryPort;
import com.smart_extract_service.modules.catalog.domain.ports.output.CatalogItemRepositoryPort;
import com.smart_extract_service.modules.catalog.domain.support.EanValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.trimToNull;


@Service
public class CatalogItemService implements CatalogItemUseCase {

    private final CatalogItemRepositoryPort itemRepository;
    private final CatalogCategoryRepositoryPort categoryRepository;
    private final AttributeDefRepositoryPort defRepository;

    public CatalogItemService(CatalogItemRepositoryPort itemRepository, CatalogCategoryRepositoryPort categoryRepository, AttributeDefRepositoryPort defRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.defRepository = defRepository;
    }

    @Override
    @Auditable
    @Transactional
    public CatalogItemResponse createItem(UUID tenantId, CatalogItemRequest request) {
        CatalogCategory category = requireCategory(request.categoryId());

        CatalogItem item = new CatalogItem();
        item.setId(UUID.randomUUID());
        item.setTenantId(tenantId);
        applyRequest(item, request);

        item.setSku(isBlank(request.sku()) ? itemRepository.nextSku(tenantId, request.itemType()) : request.sku().trim());

        validateSku(tenantId, item.getSku(), null);
        validateEan(tenantId, item.getEan(), null);

        item.setStatus(ItemStatus.DRAFT); //Rascunho
        item.ensureTypeConsistency();
        validateExtraData(category, item);

        CatalogItem saved = itemRepository.save(item);
        saveVariants(tenantId, saved.getId(), request.variants());
        return toResponse(saved, category, itemRepository.findVariants(saved.getId()));

    }

    @Override
    @Auditable
    @Transactional
    public CatalogItemResponse updateItem(UUID itemId, UUID tenantId, CatalogItemRequest request) {
        CatalogItem item = requireItem(itemId, tenantId);
        CatalogCategory category = requireCategory(request.categoryId());

        applyRequest(item, request);
        validateEan(tenantId, item.getEan(), item.getId());

        item.ensureTypeConsistency();
        validateExtraData(category, item);

        CatalogItem saved = itemRepository.save(item);
        itemRepository.deleteVariantsByItem(saved.getId());
        saveVariants(tenantId, saved.getId(), request.variants());
        return toResponse(saved, category, itemRepository.findVariants(saved.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogItemResponse findById(UUID itemId, UUID tenantId) {
        CatalogItem item = requireItem(itemId, tenantId);
        CatalogCategory category = requireCategory(item.getCategoryId());
        return toResponse(item, category, itemRepository.findVariants(item.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CatalogItemSummaryResponse> list(UUID tenantId, Map<String, String> filters, Pageable pageable) {
        Page<CatalogItem> page = itemRepository.findAll(tenantId, filters, pageable);

        Set<UUID> catIds = page.getContent().stream().map(CatalogItem::getCategoryId).collect(Collectors.toSet());
        Map<UUID, CatalogCategory> cats = categoryRepository.findByIds(catIds).stream().collect(Collectors.toMap(CatalogCategory::getId, Function.identity()));
        return page.map(item -> toSummary(item, cats.get(item.getCategoryId())));
    }

    @Override
    @Auditable
    @Transactional
    public CatalogItemResponse changeStatus(UUID itemId, UUID tenantId, ItemStatus target) {
        CatalogItem item = requireItem(itemId, tenantId);
        item.changeStatus(target);
        CatalogItem saved = itemRepository.save(item);
        return toResponse(saved, requireCategory(saved.getCategoryId()), itemRepository.findVariants(saved.getId()));
    }

    @Override
    @Auditable
    @Transactional
    public void deleteItem(UUID itemId, UUID tenantId) {
        CatalogItem item = requireItem(itemId, tenantId);
        item.softDelete();
        itemRepository.save(item);
    }

    private void applyRequest(CatalogItem item, CatalogItemRequest req) {
        item.setCategoryId(req.categoryId());
        if (item.getItemType() == null) item.setItemType(req.itemType());   // só criação
        item.setEan(trimToNull(req.ean()));
        item.setName(req.name().trim());
        item.setDescription(req.description());
        item.setUnitOfMeasure(req.unitOfMeasure());
        item.setSalePrice(req.salePrice());
        item.setCostPrice(req.costPrice());
        item.setNcm(trimToNull(req.ncm()));
        item.setCest(trimToNull(req.cest()));
        item.setTaxOrigin(trimToNull(req.taxOrigin()));
        item.setCstIcms(trimToNull(req.cstIcms()));
        item.setCsosn(trimToNull(req.csosn()));
        item.setServiceCode(trimToNull(req.serviceCode()));
        item.setIssRate(req.issRate());
        item.setTracksBatch(req.tracksBatch());
        item.setTracksExpiry(req.tracksExpiry());
        item.setHazardous(req.isHazardous());
        item.setExtraData(req.extraData() != null ? new HashMap<>(req.extraData()) : new HashMap<>());
        item.setMinStock(req.minStock());
        item.setMaxStock(req.maxStock());
        item.setReorderStock(req.reorderStock());
    }

    private CatalogCategory requireCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new BusinessException("catalog.category.not_found", "Seção do catálogo não encontrada."));
    }

    private void validateSku(UUID tenantId, String sku, UUID exceptId) {
        if (itemRepository.existsSku(tenantId, sku, exceptId)) {
            throw new BusinessException("catalog.item.sku_duplicated", "Já existe um item com o código %s. Use outro código ou deixe em branco para gerar automaticamente.".formatted(sku));
        }
    }

    private void validateEan(UUID tenantId, String ean, UUID exceptId) {
        if (ean == null) return;
        if (!EanValidator.isValid(ean)) {
            throw new BusinessException("catalog.item.ean_invalid", "O código de barras %s é inválido: confira os 13/14 dígitos e o dígito verificador.".formatted(ean));
        }
        if (itemRepository.existsEan(tenantId, ean, exceptId)) {
            throw new BusinessException("catalog.item.ean_duplicated", "Já existe um item com o código de barras %s.".formatted(ean));
        }
    }

    private void validateExtraData(CatalogCategory category, CatalogItem item) {
        for (AttributeDef def : defRepository.findByCategory(category.getId())) {
            if (!def.appliesTo(item.getItemType())) continue;
            def.validateValue(item.getExtraData().get(def.getKey()));
        }
    }

    private void saveVariants(UUID tenantId, UUID itemId, List<VariantRequest> variants) {
        if (variants == null) return;
        for (VariantRequest v : variants) {
            String sku = v.sku().trim();

            if (itemRepository.existsVariantSku(tenantId, sku, null)) {
                throw new BusinessException("catalog.variant.sku_duplicated", "Já existe uma variante com o código %s.".formatted(sku));
            }

            CatalogItemVariant variant = new CatalogItemVariant();
            variant.setId(UUID.randomUUID());
            variant.setTenantId(tenantId);
            variant.setItemId(itemId);
            variant.setSku(sku);
            variant.setEan(trimToNull(v.ean()));
            variant.setExtraData(v.extraData() != null ? new HashMap<>(v.extraData()) : new HashMap<>());
            variant.setPriceAdjustment(v.priceAdjustment() != null ? v.priceAdjustment() : BigDecimal.ZERO);
            variant.setActive(v.active() == null || v.active());
            itemRepository.saveVariant(variant);
        }
    }

    private CatalogItemResponse toResponse(CatalogItem i, CatalogCategory c, List<CatalogItemVariant> variants) {
        return new CatalogItemResponse(i.getId(), i.getSku(), i.getEan(), i.getName(), i.getDescription(), i.getItemType(), i.getStatus(), i.getCategoryId(), c.getName(), c.getVertical(), i.getUnitOfMeasure(), i.getSalePrice(), i.getCostPrice(), i.marginPercent(), i.getNcm(), i.getCest(), i.getTaxOrigin(), i.getCstIcms(), i.getCsosn(), i.getServiceCode(), i.getIssRate(), i.isTracksBatch(), i.isTracksExpiry(), i.isHazardous(), i.getExtraData(), i.getMinStock(), i.getMaxStock(), i.getReorderStock(), variants.stream().map(v -> new VariantResponse(v.getId(), v.getSku(), v.getEan(), v.getExtraData(), v.getPriceAdjustment(), v.isActive())).toList(), i.getCreatedAt(), i.getUpdatedAt());
    }

    private CatalogItem requireItem(UUID itemId, UUID tenantId) {
        CatalogItem item = itemRepository.findById(itemId).orElseThrow(() -> new BusinessException("catalog.item.not_found", "Item não encontrado."));
        if (!item.getTenantId().equals(tenantId)) {
            throw new BusinessException("catalog.item.forbidden", "Você não tem permissão para acessar este item.");
        }
        return item;
    }

    private CatalogItemSummaryResponse toSummary(CatalogItem i, CatalogCategory c) {
        return new CatalogItemSummaryResponse(i.getId(), i.getSku(), i.getEan(), i.getName(), i.getItemType(), i.getStatus(), i.getCategoryId(), c != null ? c.getName() : null, c != null ? c.getVertical() : null, i.getUnitOfMeasure(), i.getSalePrice(), i.getCostPrice(), i.marginPercent(), i.isTracksBatch(), i.isTracksExpiry(), i.isHazardous(), i.getUpdatedAt());
    }

}
