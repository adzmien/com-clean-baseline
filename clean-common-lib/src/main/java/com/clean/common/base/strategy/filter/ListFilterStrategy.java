package com.clean.common.base.strategy.filter;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.clean.common.util.JPASpecificationUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling List filter values.
 * Generates SQL IN queries for non-empty lists.
 * Returns no-op specification for empty lists.
 * Uses raw types since the strategy works for any entity type (type-agnostic).
 */
@Slf4j
@Component
@SuppressWarnings({"rawtypes", "null"})
public class ListFilterStrategy implements FilterSpecificationStrategy {

    @Override
    public Specification buildSpecification(String field, Object value, MatchMode mode) {
        List<?> listValue = (List<?>) value;

        if (listValue.isEmpty()) {
            log.debug("Empty list for field '{}', skipping", field);
            return noOp();
        }

        log.debug("Building IN filter for field '{}' with {} values", field, listValue.size());

        return JPASpecificationUtils.in(field, listValue);
    }

    @Override
    public Class<?> getSupportedType() {
        return List.class;
    }

    /**
     * Returns a no-op specification that always evaluates to true (conjunction).
     */
    private Specification noOp() {
        return (root, query, cb) -> cb.conjunction();
    }
}
