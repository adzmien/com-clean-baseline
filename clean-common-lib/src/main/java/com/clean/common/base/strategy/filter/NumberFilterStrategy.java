package com.clean.common.base.strategy.filter;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.clean.common.util.JPASpecificationUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling Number filter values.
 * Supports all Number subclasses (Integer, Long, Double, BigDecimal, etc.).
 * Always uses exact matching (MatchMode is ignored for numbers).
 * Uses raw types since the strategy works for any entity type (type-agnostic).
 */
@Slf4j
@Component
@SuppressWarnings({"rawtypes", "null"})
public class NumberFilterStrategy implements FilterSpecificationStrategy {

    @Override
    public Specification buildSpecification(String field, Object value, MatchMode mode) {
        Number numValue = (Number) value;

        log.debug("Building number filter for field '{}' with value: {}", field, numValue);

        return JPASpecificationUtils.equal(field, numValue);
    }

    @Override
    public Class<?> getSupportedType() {
        return Number.class;
    }
}
