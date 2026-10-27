package com.smart_extract_service.modules.auth.infrastructure.adapters.db.specs;

import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.UserEntity;
import com.smart_extract_service.modules.auth.infrastructure.adapters.db.entities.RoleEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<UserEntity> filterBy(Map<String, String> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            filters.forEach((key, value) -> {
                if (value != null && !value.isBlank()) {
                    switch (key) {
                        case "email":
                            predicates.add(cb.like(cb.lower(root.get("email")),
                                    "%" + value.toLowerCase() + "%"));
                            break;

                        case "active":
                            predicates.add(cb.equal(root.get("active"),
                                    Boolean.parseBoolean(value)));
                            break;

                        case "roleName":
                            Join<UserEntity, RoleEntity> roleJoin = root.join("roles");
                            predicates.add(cb.equal(roleJoin.get("name"), value));
                            break;
                        case "id":
                            predicates.add(cb.equal(root.get("id"),
                                    java.util.UUID.fromString(value)));
                            break;
                        case "search":
                            predicates.add(cb.or(
                                    cb.like(cb.lower(root.get("email")), "%" + value.toLowerCase() + "%")
                            ));
                            break;
                    }
                }
            });
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}