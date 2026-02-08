package com.clean.common.base.strategy.filter;

/**
 * Defines the matching mode for filter operations.
 * Used primarily for String filtering to distinguish between exact and fuzzy matching.
 */
public enum MatchMode {
    /**
     * Fuzzy matching mode.
     * For strings: uses LIKE with wildcards for partial, case-insensitive matching.
     * For other types: uses exact equality comparison.
     */
    FUZZY,

    /**
     * Exact matching mode.
     * Uses exact equality comparison for all types.
     */
    EXACT
}
