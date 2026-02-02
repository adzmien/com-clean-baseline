package com.clean.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import com.clean.common.base.contract.PaginationRequest;

public class PaginationUtils {

    private static final Logger log = LoggerFactory.getLogger(PaginationUtils.class);

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 200;

    private PaginationUtils() {

    }

    @NonNull
    public static Pageable getPageable(PaginationRequest request) {

        Objects.requireNonNull(request, "request must not be null");

        return getPageable(
                request.getCurrentPage(),
                request.getPageSize(),
                request.getSort(),
                null);
    }

    @NonNull
    public static Pageable getPageable(PaginationRequest request, Set<String> allowedSortFields) {

        Objects.requireNonNull(request, "request must not be null");

        return getPageable(
                request.getCurrentPage(),
                request.getPageSize(),
                request.getSort(),
                allowedSortFields);
    }

    @NonNull
    public static Pageable getPageable(Integer currentPage, Integer pageSize, String sort) {

        return getPageable(currentPage, pageSize, sort, null);
    }

    @NonNull
    public static Pageable getPageable(

            Integer currentPage,
            Integer pageSize,
            String sort,
            Set<String> allowedSortFields) {

        int safePage = sanitizePage(currentPage);
        int safeSize = sanitizeSize(pageSize);
        Sort safeSort = parseSort(sort, allowedSortFields);

        if (safeSort.isUnsorted()) {
            return PageRequest.of(safePage - 1, safeSize);
        }
        return PageRequest.of(safePage - 1, safeSize, safeSort);
    }

    private static int sanitizePage(Integer currentPage) {
        if (currentPage == null || currentPage < 1) {
            return DEFAULT_PAGE;
        }
        return currentPage;
    }

    private static int sanitizeSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_SIZE;
        }
        if (pageSize > MAX_SIZE) {
            log.warn("Requested pageSize {} exceeds max {}, clamping.", pageSize, MAX_SIZE);
            return MAX_SIZE;
        }
        return pageSize;
    }

    private static Sort parseSort(String sort, Set<String> allowedSortFields) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();

        String[] clauses = sort.split(",");
        for (String clause : clauses) {
            String trimmed = clause.trim();
            if (trimmed.isEmpty())
                continue;

            String[] parts = trimmed.split("\\s+", 2);
            String field = parts[0].trim();

            if (!isValidSortField(field)) {
                log.warn("Invalid sort field '{}', ignoring.", field);
                continue;
            }

            if (allowedSortFields != null
                    && !allowedSortFields.isEmpty()
                    && !allowedSortFields.contains(field)) {
                log.warn("Sort field '{}' not allowed, ignoring.", field);
                continue;
            }

            String orderStr = parts.length > 1 ? parts[1].trim() : "asc";
            Sort.Direction direction = parseDirection(orderStr);

            orders.add(new Sort.Order(direction, field));
        }

        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    private static boolean isValidSortField(String field) {
        return field != null && field.matches("[A-Za-z0-9_\\.]+");
    }

    private static Sort.Direction parseDirection(String orderStr) {
        if (orderStr == null || orderStr.isBlank()) {
            return Sort.Direction.ASC;
        }
        try {
            return Sort.Direction.fromString(orderStr);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid sort direction '{}', defaulting to ASC.", orderStr);
            return Sort.Direction.ASC;
        }
    }
}
