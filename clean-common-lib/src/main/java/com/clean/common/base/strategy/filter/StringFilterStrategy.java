package com.clean.common.base.strategy.filter;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.clean.common.util.JPASpecificationUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling String filter values.
 * Supports both EXACT (exact match) and FUZZY (partial, case-insensitive match) modes.
 * Uses raw types since the strategy works for any entity type (type-agnostic).
 */
@Slf4j
@Component
@SuppressWarnings({"rawtypes", "null"})
public class StringFilterStrategy implements FilterSpecificationStrategy {

    @Override
    public Specification buildSpecification(String field, Object value, MatchMode mode) {
        String strValue = (String) value;

        log.debug("Building {} string filter for field '{}' with value: {}", mode, field, strValue);

        // FUZZY -> likeIgnoreCase (partial match), EXACT -> equal (exact match)
        return (mode == MatchMode.EXACT)
                ? JPASpecificationUtils.equal(field, strValue)
                : JPASpecificationUtils.likeIgnoreCase(field, strValue);
    }

    @Override
    public Class<?> getSupportedType() {
        return String.class;
    }
}
