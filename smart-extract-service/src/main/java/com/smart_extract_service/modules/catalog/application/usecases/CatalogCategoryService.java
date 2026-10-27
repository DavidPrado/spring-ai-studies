package com.smart_extract_service.modules.catalog.application.usecases;

import com.smart_extract_service.core.context.Auditable;
import com.smart_extract_service.core.exception.BusinessException;
import com.smart_extract_service.modules.catalog.application.dto.AttributeDefRequest;
import com.smart_extract_service.modules.catalog.application.dto.AttributeDefResponse;
import com.smart_extract_service.modules.catalog.application.dto.CategoryRequest;
import com.smart_extract_service.modules.catalog.application.dto.CategoryResponse;
import com.smart_extract_service.modules.catalog.domain.enums.AppliesTo;
import com.smart_extract_service.modules.catalog.domain.model.AttributeDef;
import com.smart_extract_service.modules.catalog.domain.model.CatalogCategory;
import com.smart_extract_service.modules.catalog.domain.ports.input.CatalogCategoryUseCase;
import com.smart_extract_service.modules.catalog.domain.ports.output.AttributeDefRepositoryPort;
import com.smart_extract_service.modules.catalog.domain.ports.output.CatalogCategoryRepositoryPort;
import com.smart_extract_service.modules.catalog.domain.ports.output.CatalogItemRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CatalogCategoryService implements CatalogCategoryUseCase {
    private final CatalogCategoryRepositoryPort categoryRepository;
    private final AttributeDefRepositoryPort attributeDefRepository;
    private final CatalogItemRepositoryPort itemRepository;

    public CatalogCategoryService(CatalogCategoryRepositoryPort categoryRepository, AttributeDefRepositoryPort attributeDefRepository, CatalogItemRepositoryPort itemRepository) {
        this.categoryRepository = categoryRepository;
        this.attributeDefRepository = attributeDefRepository;
        this.itemRepository = itemRepository;
    }


    @Override
    @Auditable
    @Transactional
    public CategoryResponse createCategory(UUID tenantId, CategoryRequest request) {
        validateParent(request.parentId(), null);

        CatalogCategory category = new CatalogCategory();
        category.setId(UUID.randomUUID());
        category.setTenantId(tenantId);
        category.setName(request.name().trim());
        category.setParentId(request.parentId());
        category.setVertical(request.vertical());
        category.setActive(request.active() == null || request.active());

        return toResponse(categoryRepository.save(category), 0L);
    }

    @Override
    @Auditable
    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, UUID tenantId, CategoryRequest request) {
        CatalogCategory category = requireCategory(categoryId, tenantId);
        validateParent(request.parentId(), categoryId);

        category.setName(request.name().trim());
        category.setParentId(request.parentId());
        category.setVertical(request.vertical());

        if (request.active() != null) category.setActive(request.active());

        long items = itemRepository.countActiveByCategoryIds(List.of(categoryId))
                .getOrDefault(categoryId, 0L);

        return toResponse(categoryRepository.save(category), items);
    }

    @Override
    @Auditable
    @Transactional
    public void deleteCategory(UUID categoryId, UUID tenantId) {
        CatalogCategory category = requireCategory(categoryId, tenantId);

        long items = itemRepository.countActiveByCategoryIds(List.of(categoryId))
                .getOrDefault(categoryId, 0L);
        if(items > 0) {
            throw new BusinessException("catalog.category.has_items",
                    "Esta seção possui %d item(ns) ativo(s). Mova ou desative os itens antes de excluí-la."
                            .formatted(items));
        }
        if (categoryRepository.hasChildren(categoryId)){
            throw new BusinessException("catalog.category.has_children",
                    "Esta seção possui subseções. Exclua-as primeiro.");
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listTree(UUID tenantId, boolean activeOnly) {
        List<CatalogCategory> tree = categoryRepository.findTree(tenantId, activeOnly);
        Map<UUID, Long> counts = itemRepository.countActiveByCategoryIds(
                tree.stream().map(CatalogCategory::getId).toList());
        return tree.stream().map(c -> toResponse(c, counts.getOrDefault(c.getId(), 0L))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> list(UUID tenantId, Map<String, String> filters, Pageable pageable) {
        return categoryRepository.findAll(tenantId,filters,pageable).map(c -> toResponse(c,0L));
    }

    @Override
    @Auditable
    @Transactional
    public AttributeDefResponse saveDef(UUID tenantId, UUID categoryId, AttributeDefRequest request) {
        requireCategory(categoryId, tenantId);

        AttributeDef def = new AttributeDef();
        def.setId(UUID.randomUUID());
        def.setTenantId(tenantId);
        def.setCategoryId(categoryId);

        applyDef(def, request);

        return toResponse(attributeDefRepository.save(def));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeDefResponse> listDefs(UUID categoryId, UUID tenantId) {
        requireCategory(categoryId, tenantId);
        return attributeDefRepository.findByCategory(categoryId).stream().map(this::toResponse).toList();
    }

    @Override
    @Auditable
    @Transactional
    public void deleteDef(UUID defId, UUID tenantId) {
        AttributeDef def = attributeDefRepository.findById(defId)
                .orElseThrow(() -> new BusinessException("catalog.def.not_found", "Campo extra não encontrado."));
        if (!def.getTenantId().equals(tenantId)) {
            throw new BusinessException("catalog.def.forbidden",
                    "Você não tem permissão para alterar este campo.");
        }
        attributeDefRepository.deleteById(defId);
    }

    private void applyDef(AttributeDef def, AttributeDefRequest req) {
        String key = req.key().trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
        if (attributeDefRepository.existsKey(def.getCategoryId(), key, def.getId())) {
            throw new BusinessException("catalog.def.key_duplicated",
                    "Já existe um campo com a chave \"%s\" nesta seção.".formatted(key));
        }
        def.setKey(key);
        def.setLabel(req.label().trim());
        def.setDataType(req.dataType());
        def.setUnit(req.unit());
        def.setOptions(req.options());
        def.setRequired(req.required() != null && req.required());
        def.setAppliesTo(req.appliesTo() != null ? req.appliesTo() : AppliesTo.BOTH);
        def.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
    }


    private void validateParent(UUID parentId, UUID selfId) {
        if (parentId == null) return;
        if (parentId.equals(selfId)) {
            throw new BusinessException("catalog.category.parent_self",
                    "Uma seção não pode ser subseção dela mesma.");
        }
    }

    private CategoryResponse toResponse(CatalogCategory c, long itemCount) {
        return new CategoryResponse(c.getId(), c.getParentId(), c.getName(),
                c.getVertical(), c.isActive(), itemCount);
    }

    private AttributeDefResponse toResponse(AttributeDef d) {
        return new AttributeDefResponse(d.getId(), d.getCategoryId(), d.getKey(), d.getLabel(),
                d.getDataType(), d.getUnit(), d.getOptions(), d.isRequired(),
                d.getAppliesTo(), d.getSortOrder());
    }

    private CatalogCategory requireCategory(UUID categoryId, UUID tenantId) {
        CatalogCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("catalog.category.not_found",
                        "Seção do catálogo não encontrada."));
        if (!category.getTenantId().equals(tenantId)) {
            throw new BusinessException("catalog.category.forbidden",
                    "Você não tem permissão para acessar esta seção.");
        }
        return category;
    }
}
