package com.clean.common.base.strategy.filter;

import java.util.Map;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.clean.common.util.JPASpecificationUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategy for handling Map-based range filter values.
 * Expects maps with "min" and/or "max" keys for range queries.
 * Generates BETWEEN, >= (GTE), or <= (LTE) queries based on provided values.
 * Uses raw types since the strategy works for any entity type (type-agnostic).
 */
@Slf4j
@Component
@SuppressWarnings({"rawtypes", "unchecked", "null"})
public class RangeFilterStrategy implements FilterSpecificationStrategy {

    // Map keys for range queries
    private static final String RANGE_MIN_KEY = "min";
    private static final String RANGE_MAX_KEY = "max";

    @Override
    public Specification buildSpecification(String field, Object value, MatchMode mode) {
        Map<?, ?> range = (Map<?, ?>) value;
        return buildRangeSpecification(field, range);
    }

    @Override
    public Class<?> getSupportedType() {
        return Map.class;
    }

    /**
     * Builds a range specification from a map containing min and/or max values.
     * Extracted from DynamicFilterComponent.buildRangeSpecification().
     */
    private Specification buildRangeSpecification(String field, Map<?, ?> range) {
        // Validate field (should already be validated, but ensure @NonNull contract)
        Objects.requireNonNull(field, "Field must not be null");

        Object min = range.get(RANGE_MIN_KEY);
        Object max = range.get(RANGE_MAX_KEY);

        // If both null => no-op
        if (min == null && max == null) {
            log.debug("Range filter for field '{}' has no min/max values, skipping", field);
            return noOp();
        }

        // If both are comparable, try between
        if (min instanceof Comparable<?> && max instanceof Comparable<?>) {
            // Ensure same runtime type to avoid bad between comparisons
            if (!Objects.equals(min.getClass(), max.getClass())) {
                log.warn("Range filter for field '{}' has mismatched types: min={}, max={}. " +
                        "Using separate >= and <= instead of BETWEEN",
                    field, min.getClass().getSimpleName(), max.getClass().getSimpleName());

                Specification spec = Specification.where(null);
                spec = spec.and(buildGte(field, min));
                spec = spec.and(buildLte(field, max));
                return spec;
            }

            // Both same type and comparable - use between
            try {
                return JPASpecificationUtils.between(field, castComparable(min), castComparable(max));
            } catch (ClassCastException e) {
                log.error("Failed to cast comparable values for range on field '{}': {}", field, e.getMessage());
                throw new IllegalArgumentException(
                    "Invalid comparable values for range on field '" + field + "'", e);
            }
        }

        // Only min provided
        if (min instanceof Comparable<?>) {
            return buildGte(field, min);
        }

        // Only max provided
        if (max instanceof Comparable<?>) {
            return buildLte(field, max);
        }

        // Neither is comparable
        log.warn("Range filter for field '{}' has non-comparable values, skipping", field);
        return noOp();
    }

    /**
     * Builds a greater-than-or-equal-to specification.
     * Extracted from DynamicFilterComponent.buildGte().
     */
    private Specification buildGte(String field, Object min) {
        Objects.requireNonNull(field, "Field must not be null");
        Objects.requireNonNull(min, "Min value must not be null");
        return JPASpecificationUtils.greaterThanOrEqualTo(field, castComparable(min));
    }

    /**
     * Builds a less-than-or-equal-to specification.
     * Extracted from DynamicFilterComponent.buildLte().
     */
    private Specification buildLte(String field, Object max) {
        Objects.requireNonNull(field, "Field must not be null");
        Objects.requireNonNull(max, "Max value must not be null");
        return JPASpecificationUtils.lessThanOrEqualTo(field, castComparable(max));
    }

    /**
     * Casts an object to a Comparable type. This is necessary for range queries
     * where we need to ensure the value implements Comparable.
     * Extracted from DynamicFilterComponent.castComparable().
     *
     * @param value the value to cast (must be Comparable)
     * @return the value cast to Comparable
     * @throws ClassCastException if the value is not actually Comparable
     */
    // Caller validates value instanceof Comparable before calling
    private static <C extends Comparable<? super C>> C castComparable(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot cast null to Comparable");
        }
        if (!(value instanceof Comparable)) {
            throw new IllegalArgumentException(
                "Value of type " + value.getClass().getName() + " is not Comparable");
        }
        return (C) value;
    }

    /**
     * Returns a no-op specification that always evaluates to true (conjunction).
     */
    private Specification noOp() {
        return (root, query, cb) -> cb.conjunction();
    }
}
