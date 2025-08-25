package com.bonnysimon.starter.core.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate; // Correct Predicate import
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SearchSpecification<T> implements Specification<T> {

    private final List<SearchCriteria> criteriaList;

    // Add constructor to initialize criteriaList
    public SearchSpecification(List<SearchCriteria> criteriaList) {
        this.criteriaList = criteriaList;
    }

    @Override
    public jakarta.persistence.criteria.Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

        criteriaList.forEach(criteria -> {
            switch (criteria.operation()) {
                case ">" -> predicates.add(builder.greaterThan(root.get(criteria.key()), criteria.value().toString()));
                case "<" -> predicates.add(builder.lessThan(root.get(criteria.key()), criteria.value().toString()));
                case ":" -> {
                    if (root.get(criteria.key()).getJavaType() == String.class) {
                        predicates.add(builder.like(root.get(criteria.key()), "%" + criteria.value() + "%"));
                    } else {
                        predicates.add(builder.equal(root.get(criteria.key()), criteria.value()));
                    }
                }
            }
        });

        return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    }
}