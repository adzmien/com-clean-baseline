-- Migration: V260125001__create_initial_schema.sql
-- Description: Create initial database schema with example table
-- Author: Clean Architecture Project
-- Date: 2026-01-25

-- Create example table following project conventions
CREATE TABLE IF NOT EXISTS TBL_EXAMPLE (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    name VARCHAR(255) NOT NULL COMMENT 'Example name',
    description TEXT COMMENT 'Example description',
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING') DEFAULT 'ACTIVE' COMMENT 'Record status',
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    created_by VARCHAR(100) COMMENT 'Creator username',
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    updated_by VARCHAR(100) COMMENT 'Last updater username',
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Example table demonstrating project conventions';
