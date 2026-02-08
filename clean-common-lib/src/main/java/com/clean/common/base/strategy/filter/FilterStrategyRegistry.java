package com.clean.common.base.strategy.filter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Registry for filter specification strategies.
 * Automatically discovers and registers all FilterSpecificationStrategy beans via Spring dependency injection.
 * Provides strategy lookup based on value type with support for type hierarchy matching.
 * This registry is non-generic and shared across all entity types, with type safety maintained at method level.
 */
@Slf4j
@Component
public class FilterStrategyRegistry {

    private final Map<Class<?>, FilterSpecificationStrategy<?>> strategyMap;

    /**
     * Constructor injection of all filter strategies.
     * Spring automatically collects all beans implementing FilterSpecificationStrategy.
     *
     * @param strategies list of all strategy beans
     */
    public FilterStrategyRegistry(List<FilterSpecificationStrategy<?>> strategies) {
        this.strategyMap = new ConcurrentHashMap<>();

        strategies.forEach(strategy -> {
            Class<?> type = strategy.getSupportedType();
            strategyMap.put(type, strategy);
            log.debug("Registered filter strategy for type: {}", type.getSimpleName());
        });

        log.info("Initialized FilterStrategyRegistry with {} strategies", strategyMap.size());
    }

    /**
     * Find appropriate strategy for the given value.
     * Uses runtime type checking with inheritance support.
     * Generic type parameter ensures type-safe Specification return.
     *
     * @param <T> the entity type for the specification
     * @param value the filter value
     * @return Optional containing the strategy if found, empty otherwise
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<FilterSpecificationStrategy<T>> getStrategy(Object value) {
        if (value == null) {
            log.debug("Attempted to get strategy for null value");
            return Optional.empty();
        }

        Class<?> valueClass = value.getClass();

        // Try exact match first (most common case, fastest lookup)
        FilterSpecificationStrategy<?> strategy = strategyMap.get(valueClass);
        if (strategy != null) {
            log.debug("Found exact match strategy for type: {}", valueClass.getSimpleName());
            return Optional.of((FilterSpecificationStrategy<T>) strategy);
        }

        // Try inheritance match (for List implementations, Number subclasses, etc.)
        Optional<FilterSpecificationStrategy<T>> result = strategyMap.values().stream()
            .filter(s -> s.supports(value))
            .map(s -> (FilterSpecificationStrategy<T>) s)
            .findFirst();

        if (result.isPresent()) {
            log.debug("Found inheritance match strategy for type: {}", valueClass.getSimpleName());
        } else {
            log.debug("No strategy found for type: {}", valueClass.getSimpleName());
        }

        return result;
    }

    /**
     * Returns the number of registered strategies.
     * Useful for testing and diagnostics.
     *
     * @return count of registered strategies
     */
    public int getStrategyCount() {
        return strategyMap.size();
    }
}
