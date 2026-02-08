package com.clean.common.base.component;

import java.util.Map;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.clean.common.base.strategy.filter.FilterStrategyRegistry;
import com.clean.common.base.strategy.filter.MatchMode;
import com.clean.common.util.RequestFilterUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DynamicFilterComponent {

    private final FilterStrategyRegistry strategyRegistry;

    /**
     * Constructor injection for strategy registry.
     *
     * @param strategyRegistry the registry containing all filter strategies
     */
    public DynamicFilterComponent(FilterStrategyRegistry strategyRegistry) {
        this.strategyRegistry = strategyRegistry;
        log.debug("DynamicFilterComponent initialized with {} strategies",
            strategyRegistry.getStrategyCount());
    }

    public <T, R> Specification<T> buildSpecification(R reqData) {
        Map<String, Object> filters = RequestFilterUtil.getFilterCriteria(reqData);
        return buildSpecification(filters, MatchMode.FUZZY);
    }

    public <T, R> Specification<T> buildExactSpecification(R reqData) {
        Map<String, Object> filters = RequestFilterUtil.getFilterCriteria(reqData);
        return buildSpecification(filters, MatchMode.EXACT);
    }

    public <T> Specification<T> buildSpecification(Map<String, Object> filters) {
        return buildSpecification(filters, MatchMode.FUZZY);
    }

    public <T> Specification<T> buildExactSpecification(Map<String, Object> filters) {
        return buildSpecification(filters, MatchMode.EXACT);
    }

    private <T> Specification<T> buildSpecification(Map<String, Object> filters, MatchMode mode) {
        if (filters == null || filters.isEmpty()) {
            log.debug("No filters provided, returning empty specification");
            return noOp();
        }

        log.debug("Building {} specification with {} filter(s)", mode, filters.size());
        Specification<T> spec = noOp();

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

    private <T> Specification<T> buildSingleSpecification(String field, Object value, MatchMode mode) {
        // Validate field path
        Objects.requireNonNull(field, "Field must not be null");
        if (field.isBlank()) {
            throw new IllegalArgumentException("Field must not be blank");
        }

        // Use strategy pattern to build specification
        return strategyRegistry.<T>getStrategy(value)
            .map(strategy -> strategy.buildSpecification(field, value, mode))
            .orElseGet(() -> {
                log.warn("Unsupported value type '{}' for field '{}', skipping filter",
                    value.getClass().getName(), field);
                return noOp();
            });
    }

    private <T> Specification<T> noOp() {
        return (root, query, cb) -> cb.conjunction();
    }
}
