# DML Migrations - Data Manipulation Language

This directory contains Flyway migrations for data manipulation and reference data.

## Purpose
- Reference/master data insertion
- Data corrections and updates
- Bulk data operations
- Data migration between environments

## Naming Convention

```
V{YYMMDD}{XXX}__{description}.sql

Example: V260125005__insert_user_roles.sql
```

## File Organization
- Separate scripts by data type or purpose
- Include environment-specific data separately
- Use transaction boundaries for safety
- Coordinate version numbers with DDL migrations

## Examples
- `V260125005__insert_user_roles.sql` - Standard user roles
- `V260125006__insert_system_config.sql` - System configuration
- `V260126003__update_legacy_user_status.sql` - Legacy data corrections
- `V260126004__insert_country_reference.sql` - Country/region data

## Best Practices

### 1. Migration Headers
Always include a header comment:
```sql
-- Migration: V260125005__insert_user_roles.sql
-- Description: Insert standard user roles (admin, user, guest)
-- Author: Clean Architecture Project
-- Date: 2026-01-25
```

### 2. Reference Data Example
```sql
-- Insert standard user roles
INSERT INTO TBL_USER_ROLE (role_name, description, permissions, created_on, created_by) VALUES
('ADMIN', 'System administrator with full access', 'ALL', CURRENT_TIMESTAMP, 'SYSTEM'),
('USER', 'Standard user with basic access', 'READ,WRITE', CURRENT_TIMESTAMP, 'SYSTEM'),
('GUEST', 'Guest user with read-only access', 'READ', CURRENT_TIMESTAMP, 'SYSTEM');
```

### 3. Use Transactions
Wrap DML operations in transactions for safety:
```sql
START TRANSACTION;

-- Your DML statements here
UPDATE TBL_USER SET status = 'ACTIVE' WHERE status = 'PENDING';

COMMIT;
```

### 4. Idempotency
Make migrations idempotent when possible:
```sql
-- Safe to run multiple times
INSERT IGNORE INTO TBL_CONFIG (config_key, config_value) VALUES
('max_login_attempts', '3'),
('session_timeout', '30');

-- Or use ON DUPLICATE KEY UPDATE
INSERT INTO TBL_CONFIG (config_key, config_value) VALUES
('max_login_attempts', '3')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
```

### 5. Data Validation
Include validation queries before major updates:
```sql
-- Validate data before update
SELECT COUNT(*) INTO @count FROM TBL_USER WHERE status IS NULL;

-- Only proceed if validation passes
UPDATE TBL_USER SET status = 'INACTIVE' WHERE status IS NULL AND @count > 0;
```

### 6. Rollback Consideration
For each DML migration, create a corresponding rollback script in:
`/rollback/{YYMMDD}/V{YYMMDD}{XXX}__rollback_{description}.sql`

Example rollback for data correction:
```sql
-- Rollback: Restore original status values
UPDATE TBL_USER SET status = 'PENDING' WHERE status = 'INACTIVE' AND updated_on >= '2026-01-25';
```

## Deployment

Safe for production when using:
```bash
./gradlew flywayMigrateDml
./gradlew flywayMigrateDdlDml  # Recommended for production
```

## Important Notes

- **Never include production credentials** in DML scripts
- **Test thoroughly** in development before production deployment
- **Consider data volume** for performance implications
- **Document dependencies** between DML and DDL migrations
- **Use appropriate version numbers** to ensure correct execution order
