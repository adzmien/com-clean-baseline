# DDL Migrations - Data Definition Language

This directory contains Flyway migrations for database schema definitions.

## Purpose
- Table creation and structure modifications
- Index and constraint definitions
- View and stored procedure definitions
- Database object management

## Naming Convention

```
V{YYMMDD}{XXX}__{description}.sql

Example: V260125001__create_users_table.sql
```

## File Organization
- Group related tables by domain/module
- Use descriptive names indicating the action
- Follow date-based versioning for chronological order

## Examples
- `V260125001__create_users_table.sql` - Create users table
- `V260125002__create_orders_table.sql` - Create orders table
- `V260126001__add_users_email_index.sql` - Add email index to users
- `V260126002__alter_orders_add_status.sql` - Add status column to orders

## Best Practices

### 1. Table Creation
- Always use InnoDB engine
- Set charset to utf8mb4
- Include audit fields (created_on, created_by, updated_on, updated_by)
- Add appropriate indexes
- Include table and column comments

### 2. Migration Headers
Always include a header comment:
```sql
-- Migration: V260125001__create_users_table.sql
-- Description: Create users table with authentication fields
-- Author: Clean Architecture Project
-- Date: 2026-01-25
```

### 3. Table Structure Example
```sql
CREATE TABLE IF NOT EXISTS TBL_USER (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique username',
    email VARCHAR(255) NOT NULL COMMENT 'User email address',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT 'User status',
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    created_by VARCHAR(100) COMMENT 'Creator username',
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update timestamp',
    updated_by VARCHAR(100) COMMENT 'Last updater username',
    UNIQUE KEY uk_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User accounts table';
```

### 4. Indexes
- Prefix unique keys with `uk_`
- Prefix regular indexes with `idx_`
- Create composite indexes for multi-column queries
- Index foreign keys for join performance

### 5. Rollback Consideration
For each DDL migration, create a corresponding rollback script in:
`/rollback/{YYMMDD}/V{YYMMDD}{XXX}__rollback_{description}.sql`

## Deployment

Safe for production when using:
```bash
./gradlew flywayMigrateDdl
./gradlew flywayMigrateDdlDml  # Recommended for production
```
