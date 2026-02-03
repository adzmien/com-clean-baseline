package com.clean.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility for extracting filter criteria from request objects using reflection.
 * Thread-safe with field caching for performance.
 */
public final class RequestFilterUtil {

    private static final Logger log = LoggerFactory.getLogger(RequestFilterUtil.class);

    // Exclude pagination and serialization fields by default
    private static final Set<String> DEFAULT_EXCLUDED_FIELDS = Set.of(
            "serialVersionUID",
            "currentPage",
            "pageSize",
            "sort");

    // Thread-safe cache for reflection field lookups
    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    // Prevent memory leaks from excessive dynamic class loading
    private static final int MAX_CACHE_SIZE = 1000;

    private RequestFilterUtil() {

        throw new AssertionError("Utility class should not be instantiated");
    }

    public static Map<String, Object> getFilterCriteria(Object obj) {
        return getFilterCriteria(obj, DEFAULT_EXCLUDED_FIELDS);
    }

    public static Map<String, Object> getFilterCriteria(Object obj, Set<String> excludedFields) {
        Set<String> exclusions = excludedFields != null ? excludedFields : Collections.emptySet();
        return getFilterCriteria(obj, field -> !exclusions.contains(field.getName()));
    }

    /**
     * Extracts filter criteria from an object using reflection and a custom field filter.
     *
     * @param obj the object to extract filters from
     * @param fieldFilter predicate to determine which fields to include
     * @return immutable map of field name to value (only non-null values)
     * @throws NullPointerException if fieldFilter is null
     */
    public static Map<String, Object> getFilterCriteria(Object obj, Predicate<Field> fieldFilter) {
        if (obj == null) {
            return Collections.emptyMap();
        }

        Objects.requireNonNull(fieldFilter, "Field filter predicate must not be null");

        Map<String, Object> result = new LinkedHashMap<>();
        List<Field> fields = getCachedFields(obj.getClass());

        for (Field field : fields) {
            // Skip compiler-generated synthetic fields
            if (field.isSynthetic()) {
                continue;
            }

            // Apply custom filter predicate
            if (!fieldFilter.test(field)) {
                continue;
            }

            // Extract field value, skip if null
            Object value = readFieldSafely(field, obj);
            if (value != null) {
                result.put(field.getName(), value);
            }
        }

        return result.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(result);
    }

    private static List<Field> getCachedFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, k -> {

            if (FIELD_CACHE.size() > MAX_CACHE_SIZE) {
                log.warn("Field cache exceeded maximum size ({}). Clearing cache to prevent memory leak. " +
                        "This may indicate excessive dynamic class loading.", MAX_CACHE_SIZE);
                FIELD_CACHE.clear();
            }

            return Collections.unmodifiableList(getAllFields(clazz));
        });
    }

    /**
     * Recursively collects all instance fields from a class and its parent classes.
     * Excludes static fields as they are not instance-specific.
     *
     * @param type the class to inspect
     * @return list of all instance fields (includes inherited fields)
     */
    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;

        // Traverse class hierarchy up to Object (excluded)
        while (current != null && current != Object.class) {
            // Filter out static fields - they're not instance-specific
            List<Field> instanceFields = Arrays.stream(current.getDeclaredFields())
                    .filter(f -> !Modifier.isStatic(f.getModifiers()))
                    .collect(Collectors.toList());

            fields.addAll(instanceFields);
            current = current.getSuperclass();
        }

        return fields;
    }

    /**
     * Safely reads a field value using reflection, handling access restrictions gracefully.
     * Attempts to make field accessible if needed (Java 9+ module restrictions).
     *
     * @param field the field to read
     * @param target the object instance to read from
     * @return the field value, or null if inaccessible
     * @throws IllegalArgumentException if target is invalid or unexpected error occurs
     */
    private static Object readFieldSafely(Field field, Object target) {
        try {
            // Check and handle Java 9+ module access restrictions
            if (!field.canAccess(target)) {
                if (!field.trySetAccessible()) {
                    // Expected for module-protected fields, not an error
                    log.debug("Skipping inaccessible field '{}' on '{}'. " +
                            "This may be due to module access restrictions.",
                            field.getName(), target.getClass().getName());
                    return null;
                }
            }

            return field.get(target);

        } catch (IllegalAccessException e) {
            // Field not accessible despite trySetAccessible - skip silently
            log.debug("Could not access field '{}' on '{}': {}",
                    field.getName(), target.getClass().getName(), e.getMessage());
            return null;

        } catch (IllegalArgumentException e) {
            // Target object doesn't match field's declaring class - programming error
            String msg = String.format(
                    "Invalid target object for field '%s' on class '%s'",
                    field.getName(), target.getClass().getName());
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);

        } catch (Exception e) {
            // Unexpected error - propagate as IllegalArgumentException
            String msg = String.format(
                    "Unexpected error reading field '%s' from '%s'",
                    field.getName(), target.getClass().getName());
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }

    public static void clearCache() {
        FIELD_CACHE.clear();
        log.debug("Field cache cleared");
    }

    public static int getCacheSize() {
        return FIELD_CACHE.size();
    }
}
