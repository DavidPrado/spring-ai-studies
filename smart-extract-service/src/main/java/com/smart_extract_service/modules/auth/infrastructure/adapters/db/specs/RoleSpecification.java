package com.smart_extract_service.modules.auth.infrastructure.adapters.db.specs;

import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.RoleEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RoleSpecification {

    private RoleSpecification() {}

    public static Specification<RoleEntity> filterBy(UUID tenantId, Map<String, String> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tenantId != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("tenant").get("id"), tenantId),
                        cb.isTrue(root.get("isSystemDefault"))
                ));
            }

            // ✅ ADICIONADO: filtro por name e displayName (consistência com filterGlobal)
            if (filters != null) {
                String nameFilter = filters.get("name");
                if (nameFilter != null && !nameFilter.isBlank()) {
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + nameFilter.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("displayName")), "%" + nameFilter.toLowerCase() + "%")
                    ));
                }
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<RoleEntity> filterGlobal(Map<String, String> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters != null) {
                String nameFilter = filters.get("name");
                if (nameFilter != null && !nameFilter.isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("name")),
                            "%" + nameFilter.toLowerCase() + "%"));
                }
                String df = filters.get("displayName");
                if (df != null && !df.isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("displayName")),
                            "%" + df.toLowerCase() + "%"));
                }
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}