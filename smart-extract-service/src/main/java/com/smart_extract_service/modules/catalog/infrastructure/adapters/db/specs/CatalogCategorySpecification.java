package com.smart_extract_service.modules.catalog.infrastructure.adapters.db.specs;

import com.smart_extract_service.modules.catalog.domain.enums.Vertical;
import com.smart_extract_service.modules.catalog.infrastructure.adapters.db.entities.CatalogCategoryEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public final class CatalogCategorySpecification {

    private CatalogCategorySpecification() {}

    public static Specification<CatalogCategoryEntity> filterBy(UUID tenantId, Map<String, String> filters) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));

            if (filters != null) {
                String name = filters.get("name");
                if (name != null && !name.isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("name")),
                            "%" + name.trim().toLowerCase() + "%"));
                }
                String vertical = filters.get("vertical");
                if (vertical != null && !vertical.isBlank()) {
                    predicates.add(cb.equal(root.get("vertical"), Vertical.valueOf(vertical)));
                }
                String active = filters.get("active");
                if (active != null && !active.isBlank()) {
                    predicates.add(cb.equal(root.get("active"), Boolean.parseBoolean(active)));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}