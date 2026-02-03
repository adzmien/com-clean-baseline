package com.clean.common.util;

import java.util.Collection;
import java.util.function.Function;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JPASpecificationUtils {

    private static final char LIKE_ESCAPE = '\\';

    private JPASpecificationUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static <T, V> Specification<T> equal(
            Function<Root<T>, Path<V>> path,
            V value) {

        return (root, query, cb) ->
                value == null ? cb.conjunction() : cb.equal(path.apply(root), value);
    }

    public static <T> Specification<T> likeIgnoreCase(
            Function<Root<T>, Path<String>> path,
            String value) {

        return (root, query, cb) -> {
            if (value == null || value.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + escapeLike(value.trim().toLowerCase()) + "%";
            return cb.like(
                    cb.lower(path.apply(root)),
                    pattern,
                    LIKE_ESCAPE
            );
        };
    }

    public static <T, V> Specification<T> in(
            Function<Root<T>, Path<V>> path,
            Collection<V> values) {

        return (root, query, cb) ->
                (values == null || values.isEmpty())
                        ? cb.conjunction()
                        : path.apply(root).in(values);
    }

    public static <T, V extends Comparable<? super V>> Specification<T> greaterThanOrEqualTo(
            Function<Root<T>, Path<V>> path,
            V value) {

        return (root, query, cb) ->
                value == null ? cb.conjunction() : cb.greaterThanOrEqualTo(path.apply(root), value);
    }

    public static <T, V extends Comparable<? super V>> Specification<T> lessThanOrEqualTo(
            Function<Root<T>, Path<V>> path,
            V value) {

        return (root, query, cb) ->
                value == null ? cb.conjunction() : cb.lessThanOrEqualTo(path.apply(root), value);
    }

    public static <T, V extends Comparable<? super V>> Specification<T> between(
            Function<Root<T>, Path<V>> path,
            V from,
            V to) {

        if (from != null && to != null) {
            return (root, query, cb) -> cb.between(path.apply(root), from, to);
        }
        if (from != null) {
            return greaterThanOrEqualTo(path, from);
        }
        if (to != null) {
            return lessThanOrEqualTo(path, to);
        }
        return (root, query, cb) -> cb.conjunction();
    }

    // ==================== String-based overloads ====================
    // These methods accept String field paths instead of Function-based paths,
    // useful for dynamic filtering where field names come from user input

    public static <T, V> Specification<T> equal(@NonNull String fieldPath, V value) {
        return (root, query, cb) -> {
            if (value == null) {
                return cb.conjunction();
            }
            Path<V> path = resolvePath(root, fieldPath);
            return cb.equal(path, value);
        };
    }

    public static <T> Specification<T> likeIgnoreCase(@NonNull String fieldPath, String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) {
                return cb.conjunction();
            }
            Path<String> path = resolvePath(root, fieldPath);
            String pattern = "%" + escapeLike(value.trim().toLowerCase()) + "%";
            return cb.like(cb.lower(path), pattern, LIKE_ESCAPE);
        };
    }

    public static <T, V> Specification<T> in(@NonNull String fieldPath, Collection<V> values) {
        return (root, query, cb) -> {
            if (values == null || values.isEmpty()) {
                return cb.conjunction();
            }
            Path<V> path = resolvePath(root, fieldPath);
            return path.in(values);
        };
    }

    public static <T, V extends Comparable<? super V>> Specification<T> greaterThanOrEqualTo(
            @NonNull String fieldPath, V value) {
        return (root, query, cb) -> {
            if (value == null) {
                return cb.conjunction();
            }
            Path<V> path = resolvePath(root, fieldPath);
            return cb.greaterThanOrEqualTo(path, value);
        };
    }

    public static <T, V extends Comparable<? super V>> Specification<T> lessThanOrEqualTo(
            @NonNull String fieldPath, V value) {
        return (root, query, cb) -> {
            if (value == null) {
                return cb.conjunction();
            }
            Path<V> path = resolvePath(root, fieldPath);
            return cb.lessThanOrEqualTo(path, value);
        };
    }

    public static <T, V extends Comparable<? super V>> Specification<T> between(
            @NonNull String fieldPath, V from, V to) {

        if (from != null && to != null) {
            return (root, query, cb) -> {
                Path<V> path = resolvePath(root, fieldPath);
                return cb.between(path, from, to);
            };
        }
        if (from != null) {
            return greaterThanOrEqualTo(fieldPath, from);
        }
        if (to != null) {
            return lessThanOrEqualTo(fieldPath, to);
        }
        return (root, query, cb) -> cb.conjunction();
    }

    /**
     * Resolves a field path from a root entity, supporting nested paths with dot notation.
     * Example: "address.city" will traverse address relationship and get city field.
     *
     * @param root the JPA root entity
     * @param fieldPath the field path (supports dot notation for nested fields)
     * @return the resolved Path
     * @throws IllegalArgumentException if the field path is invalid
     */
    @SuppressWarnings("unchecked") // Safe: Path navigation is type-checked by JPA at runtime
    private static <T, V> Path<V> resolvePath(Root<T> root, String fieldPath) {
        if (fieldPath == null || fieldPath.isBlank()) {
            throw new IllegalArgumentException("Field path must not be null or blank");
        }

        String[] pathParts = fieldPath.split("\\.");
        Path<?> path = root;

        for (String part : pathParts) {
            if (part.isBlank()) {
                log.warn("Invalid field path '{}' contains empty segment", fieldPath);
                throw new IllegalArgumentException("Field path '" + fieldPath + "' contains empty segment");
            }
            try {
                path = path.get(part);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid field path '{}': field '{}' not found", fieldPath, part);
                throw new IllegalArgumentException(
                    "Invalid field path '" + fieldPath + "': field '" + part + "' not found", e);
            }
        }

        return (Path<V>) path;
    }

    private static String escapeLike(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
