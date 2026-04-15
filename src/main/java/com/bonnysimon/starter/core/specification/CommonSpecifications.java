package com.bonnysimon.starter.core.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.Arrays;


public class CommonSpecifications {

    public static <T> Specification<T> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    /**
     * Search on multiple fields dynamically
     */
    @SafeVarargs
    public static <T> Specification<T> withSearch(String search, String... fields) {
        return (root, query, cb) -> {
            var notDeleted = cb.isFalse(root.get("deleted"));

            if (search == null || search.trim().isEmpty() || fields.length == 0) {
                return notDeleted;
            }

            String pattern = "%" + search.trim().toLowerCase() + "%";

            Predicate[] predicates = Arrays.stream(fields)
                    .map(field -> cb.like(cb.lower(root.get(field)), pattern))
                    .toArray(Predicate[]::new);

            return cb.and(notDeleted, cb.or(predicates));
        };
    }
}