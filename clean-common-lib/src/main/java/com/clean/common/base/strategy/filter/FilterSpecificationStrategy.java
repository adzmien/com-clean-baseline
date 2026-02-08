package com.clean.common.base.strategy.filter;

import org.springframework.data.jpa.domain.Specification;

/**
 * Strategy interface for building JPA Specifications based on filter values.
 * Each implementation handles a specific value type (String, Number, Boolean, List, Map, etc.).
 *
 * @param <T> the entity type for the specification
 */
public interface FilterSpecificationStrategy<T> {

    /**
     * Builds a JPA Specification for the given field and value.
     *
     * @param field the field path (supports nested paths via dot notation, e.g., "address.city")
     * @param value the filter value (non-null, type-specific)
     * @param mode the match mode (FUZZY or EXACT)
     * @return JPA Specification for the filter
     * @throws IllegalArgumentException if the field or value is invalid
     */
    Specification<T> buildSpecification(String field, Object value, MatchMode mode);

    /**
     * Returns the Java type this strategy handles.
     * Used for strategy selection based on value type.
     *
     * @return the supported value type
     */
    Class<?> getSupportedType();

    /**
     * Validates if this strategy can handle the given value.
     * Default implementation checks if the value is an instance of the supported type.
     *
     * @param value the value to check
     * @return true if this strategy can handle the value
     */
    default boolean supports(Object value) {
        return value != null && getSupportedType().isAssignableFrom(value.getClass());
    }
}
