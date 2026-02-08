package com.clean.common.base.strategy.filter;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.clean.common.util.JPASpecificationUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling Boolean filter values.
 * Always uses exact matching (MatchMode is ignored for booleans).
 * Uses raw types since the strategy works for any entity type (type-agnostic).
 */
@Slf4j
@Component
@SuppressWarnings({"rawtypes", "null"})
public class BooleanFilterStrategy implements FilterSpecificationStrategy {

    @Override
    public Specification buildSpecification(String field, Object value, MatchMode mode) {
        Boolean boolValue = (Boolean) value;

        log.debug("Building boolean filter for field '{}' with value: {}", field, boolValue);

        return JPASpecificationUtils.equal(field, boolValue);
    }

    @Override
    public Class<?> getSupportedType() {
        return Boolean.class;
    }
}
