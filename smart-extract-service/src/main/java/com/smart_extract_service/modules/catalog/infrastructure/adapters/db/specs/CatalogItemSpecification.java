package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.specs;

import com.smart_extract_service.modules.catalog.domain.enums.ItemStatus;
import com.smart_extract_service.modules.catalog.domain.enums.ItemType;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities.CatalogItemEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import jakarta.persistence.criteria.Predicate;


public final class CatalogItemSpecification {

    CatalogItemSpecification() {}

    public static Specification<CatalogItemEntity> filterBy(UUID tenantId, Map<String, String> filters){
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filters != null){
                String search = filters.getOrDefault("search", filters.get("name"));
                if(search != null && !search.isBlank()){
                    String like = "%" + search.trim().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")),like),
                            cb.like(cb.lower(root.get("sku")), like)
                    ));
                }
                if (has(filters, "categoryId")){
                    predicates.add(cb.equal(root.get("categoryId"), UUID.fromString(filters.get("categoryId"))));
                }
                if(has(filters, "status")){
                    predicates.add(cb.equal(root.get("status"), ItemStatus.valueOf(filters.get("status"))));
                }
                if (has(filters, "itemType")){
                    predicates.add(cb.equal(root.get("itemType"), ItemType.valueOf(filters.get("itemType"))));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

    }

    private static boolean has(Map<String,String> f, String key){
        return f != null && f.get(key) != null && !f.get(key).isBlank();
    }
}
