package com.clean.common.base.component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.clean.common.util.JPASpecificationUtils;
import com.clean.common.util.RequestFilterUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DynamicFilterComponent<T> {

    // Map keys for range queries
    private static final String RANGE_MIN_KEY = "min";
    private static final String RANGE_MAX_KEY = "max";

    public <R> Specification<T> buildSpecification(R reqData) {
        Map<String, Object> filters = RequestFilterUtil.getFilterCriteria(reqData);
        return buildSpecification(filters, MatchMode.FUZZY);
    }

    public <R> Specification<T> buildExactSpecification(R reqData) {
        Map<String, Object> filters = RequestFilterUtil.getFilterCriteria(reqData);
        return buildSpecification(filters, MatchMode.EXACT);
    }

    public Specification<T> buildSpecification(Map<String, Object> filters) {
        return buildSpecification(filters, MatchMode.FUZZY);
    }

    public Specification<T> buildExactSpecification(Map<String, Object> filters) {
        return buildSpecification(filters, MatchMode.EXACT);
    }

    private Specification<T> buildSpecification(Map<String, Object> filters, MatchMode mode) {
        if (filters == null || filters.isEmpty()) {
            log.debug("No filters provided, returning empty specification");
            return Specification.where(null);
        }

        log.debug("Building {} specification with {} filter(s)", mode, filters.size());
        Specification<T> spec = Specification.where(null);

        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();

            // Skip invalid entries
            if (field == null || field.isBlank()) {
                log.warn("Skipping filter with null or blank field name");
                continue;
            }

            if (value == null) {
                log.debug("Skipping filter for field '{}' with null value", field);
                continue;
            }

            try {
                Specification<T> fieldSpec = buildSingleSpecification(field, value, mode);
                spec = spec.and(fieldSpec);
                log.debug("Added {} filter for field '{}' with value type: {}",
                    mode, field, value.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("Failed to build specification for field '{}': {}", field, e.getMessage(), e);
                throw new IllegalArgumentException(
                    "Failed to build specification for field '" + field + "': " + e.getMessage(), e);
            }
        }

        return spec;
    }

    private Specification<T> buildSingleSpecification(String field, Object value, MatchMode mode) {
        // Validate field path
        Objects.requireNonNull(field, "Field must not be null");
        if (field.isBlank()) {
            throw new IllegalArgumentException("Field must not be blank");
        }

        if (value instanceof String strValue) {
            // FUZZY -> likeIgnoreCase (partial match), EXACT -> equal (exact match)
            return (mode == MatchMode.EXACT)
                    ? JPASpecificationUtils.equal(field, strValue)
                    : JPASpecificationUtils.likeIgnoreCase(field, strValue);
        }

        if (value instanceof Boolean boolValue) {
            return JPASpecificationUtils.equal(field, boolValue);
        }

        if (value instanceof Number numValue) {
            return JPASpecificationUtils.equal(field, numValue);
        }

        if (value instanceof List<?> listValue) {
            if (listValue.isEmpty()) {
                log.debug("Empty list for field '{}', skipping", field);
                return noOp();
            }
            return JPASpecificationUtils.in(field, listValue);
        }

        if (value instanceof Map<?, ?> mapValue) {
            return buildRangeSpecification(field, mapValue);
        }

        // Unsupported type - log warning and skip
        log.warn("Unsupported value type '{}' for field '{}', skipping filter",
            value.getClass().getName(), field);
        return noOp();
    }

    private Specification<T> buildRangeSpecification(String field, Map<?, ?> range) {
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

                Specification<T> spec = Specification.where(null);
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

    private Specification<T> buildGte(String field, Object min) {
        Objects.requireNonNull(field, "Field must not be null");
        Objects.requireNonNull(min, "Min value must not be null");
        return JPASpecificationUtils.greaterThanOrEqualTo(field, castComparable(min));
    }

    private Specification<T> buildLte(String field, Object max) {
        Objects.requireNonNull(field, "Field must not be null");
        Objects.requireNonNull(max, "Max value must not be null");
        return JPASpecificationUtils.lessThanOrEqualTo(field, castComparable(max));
    }

    /**
     * Casts an object to a Comparable type. This is necessary for range queries
     * where we need to ensure the value implements Comparable.
     *
     * @param value the value to cast (must be Comparable)
     * @return the value cast to Comparable
     * @throws ClassCastException if the value is not actually Comparable
     */
    @SuppressWarnings("unchecked") // Caller validates value instanceof Comparable before calling
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

    private Specification<T> noOp() {
        return (root, query, cb) -> cb.conjunction();
    }

    private enum MatchMode {
        FUZZY,
        EXACT
    }
}
