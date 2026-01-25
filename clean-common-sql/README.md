# Clean SQL Migration Management

This module manages database schema migrations for the Clean Architecture project using Flyway with a date-based versioning system.

## Project Structure

```
clean-common-sql/
├── src/main/resources/db/migration/sql/
│   ├── ddl-dml/      # Data Definition & Manipulation Language (schema and reference data)
│   ├── dummy/        # Test/development sample data
│   └── rollback/     # Date-organized rollback scripts
│       └── {YYMMDD}/ # Rollback scripts organized by migration date
```

## Migration Naming Convention

Migrations follow the date-based versioning pattern:

```
V{YYMMDD}{XXX}__{description}.sql

Where:
- V = Flyway version prefix (required)
- YYMMDD = Year/Month/Day (e.g., 260125 for Jan 25, 2026)
- XXX = Sequential number for same day (001, 002, 003...)
- __ = Double underscore separator (required by Flyway)
- description = Descriptive name of the migration
```

### Examples:
- `V260125001__create_initial_schema.sql` - First migration on Jan 25, 2026
- `V260125002__insert_sample_data.sql` - Second migration on Jan 25, 2026
- `V260126001__alter_table_add_column.sql` - First migration on Jan 26, 2026

## Quick Command Reference

| Command | Purpose | Safe for Prod? |
|---------|---------|----------------|
| `./gradlew flywayInfo` | Check production migration status | ✅ Yes |
| `./gradlew flywayInfoDummy` | Check dummy migration status | ℹ️ Dev only |
| `./gradlew flywayValidate` | Verify production migration integrity | ✅ Yes |
| `./gradlew flywayValidateDummy` | Verify dummy migration integrity | ℹ️ Dev only |
| `./gradlew flywayMigrate` | Apply all migrations (DDL+DML+dummy) | ⚠️ No |
| `./gradlew flywayMigrateDdlDml` | Apply DDL+DML only (no dummy) | ✅ **Recommended** |
| `./gradlew flywayMigrateDummy` | Apply dummy data only (separate history) | ❌ No |
| `./gradlew flywayRepair` | Fix production migration issues | ⚠️ Careful |
| `./gradlew flywayBaseline` | Initialize production migration tracking | ⚠️ Careful |
| `./gradlew flywayRollback` | Interactive rollback by date | ⚠️ Careful |
| `./gradlew flywayClean` | Drop all database objects | ❌ **NEVER** |
| `./gradlew flywayCleanDummy` | Drop dummy history table only | ℹ️ Dev only |

**Default Configuration:**
- Database: MariaDB
- Schema: `clean_dev`
- User: `root`
- Connection: `jdbc:mariadb://localhost:3306/clean_dev`

See `gradle.properties` for current settings or override via `-P` flags (see Database Configuration section).

## Prerequisites

- JDK 17 or higher
- Gradle 7.6+ or 8.x
- MariaDB or MySQL database instance
- Database schema created (e.g., `CREATE DATABASE clean_dev;`)

## Database Configuration

**Target Database:** MariaDB
**Schema:** `clean_dev`
**Connection:** `jdbc:mariadb://localhost:3306/clean_dev`

### Configuration Properties

You can override default connection settings via gradle properties:

```bash
./gradlew flywayMigrate -Pflyway.url=jdbc:mariadb://localhost:3306/clean_prod
./gradlew flywayMigrate -Pflyway.user=admin -Pflyway.password=secret
```

**Available properties:**
- `flyway.url` - JDBC connection URL
- `flyway.user` - Database username
- `flyway.password` - Database password
- `flyway.cleanDisabled` - Safety flag for clean command (default: true)

### Editing gradle.properties

Edit `gradle.properties` to set your database credentials:

```properties
flyway.url=jdbc:mariadb://localhost:3306/clean_dev
flyway.user=root
flyway.password=your_password_here
flyway.cleanDisabled=true
```

## Understanding Migration History Tables

This project uses **two separate Flyway schema history tables** to keep production and test data migrations isolated:

### Production History: `flyway_schema_history`
- Tracks DDL and DML migrations from `db/migration/sql/ddl-dml/`
- Used by production deployments
- Accessed by: `flywayMigrate`, `flywayMigrateDdlDml`, `flywayInfo`, `flywayValidate`
- **Critical:** Never modify this table manually

### Dummy/Test History: `flyway_schema_history_dummy`
- Tracks dummy data migrations from `db/migration/sql/dummy/`
- Used only in development/test environments
- Accessed by: `flywayMigrateDummy`, `flywayInfoDummy`, `flywayValidateDummy`
- Safe to reset using `flywayCleanDummy` task

### Why Two Tables?

**Problem:** Running dummy migrations writes test data records into the production migration history, causing:
- Polluted migration history with non-production entries
- Confusion about which migrations are actually deployed in production
- Risk of accidentally deploying test data to production

**Solution:** Dummy migrations use a separate history table (`flyway_schema_history_dummy`), keeping:
- Production history clean and auditable
- Test data migrations tracked separately
- Clear separation of concerns

### Checking Migration Status

```bash
# Check production migrations
./gradlew flywayInfo
# Shows: flyway_schema_history entries (DDL/DML only)

# Check dummy migrations
./gradlew flywayInfoDummy
# Shows: flyway_schema_history_dummy entries (test data only)
```

### Best Practice

**For Production/Staging:**
```bash
./gradlew flywayMigrateDdlDml  # Only writes to flyway_schema_history
```

**For Development:**
```bash
./gradlew flywayMigrateDdlDml  # First apply production migrations
./gradlew flywayMigrateDummy   # Then apply dummy data (separate history)
```

## Migration Commands

### Quick Start

```bash
# Navigate to the project directory
cd /Users/adzmien/Workspace/code/github/com-clean-baseline/clean-common-sql

# Check Gradle version
./gradlew --version

# Check migration status
./gradlew flywayInfo
```

### Standard Flyway Commands

#### 1. **Info** - Check Migration Status
View current migration status, pending migrations, and history:

```bash
./gradlew flywayInfo
```

**Output includes:**
- ✅ Applied migrations (version, description, timestamp, checksum)
- ⏳ Pending migrations (not yet executed)
- ❌ Failed migrations (if any)
- 📊 Migration summary (success count, pending count)

**Example output:**
```
+-----------+---------+---------------------+------+---------------------+
| Category  | Version | Description         | Type | Installed On        |
+-----------+---------+---------------------+------+---------------------+
| Success   | 260125001 | create initial schema | SQL  | 2026-01-25 10:30:15 |
| Pending   | 260125002 | insert sample data    | SQL  |                     |
+-----------+---------+---------------------+------+---------------------+
```

#### 2. **Migrate** - Apply Pending Migrations
Execute all pending migrations (DDL + DML + dummy data):

```bash
./gradlew flywayMigrate
```

**What it does:**
- Scans migration folders (ddl-dml/, dummy/)
- Applies migrations in version order
- Updates flyway_schema_history table
- Validates checksums for existing migrations

#### 3. **Validate** - Verify Migration Integrity
Verify that applied migrations match their checksums:

```bash
./gradlew flywayValidate
```

**Use when:**
- Detecting if migration files were modified after execution
- Ensuring migration consistency across environments
- Troubleshooting migration issues

#### 4. **Baseline** - Initialize Migration Tracking
Mark an existing database at a specific version without executing migrations:

```bash
./gradlew flywayBaseline
```

**Use when:**
- Adding Flyway to an existing database
- Starting migration tracking from current state
- Skipping initial migrations that already exist

#### 5. **Repair** - Fix Migration Issues
Repairs the schema history table:

```bash
./gradlew flywayRepair
```

**What it fixes:**
- ✅ Removes failed migration entries
- ✅ Realigns checksums for applied migrations
- ✅ Clears checksum mismatches

**Use when:**
- Migration failed partway through
- Checksum validation errors occur
- Schema history table is corrupted

#### 6. **Clean** - Drop All Database Objects
⚠️ **DANGEROUS**: Drops all objects in configured schemas:

```bash
./gradlew flywayClean -Pflyway.cleanDisabled=false
```

**Safety:**
- Disabled by default (`cleanDisabled=true`)
- Must explicitly enable via property
- **NEVER use in production**

**Use only for:**
- Development database reset
- Running fresh migrations in test environments
- Cleaning up after failed tests

### Custom Migration Commands

#### 1. **Migrate Dummy Data Only**
Deploy dummy/test data only (development sample data):

```bash
./gradlew flywayMigrateDummy
```

**What it does:**
- Applies migrations from `db/migration/sql/dummy/` folder
- Records history in **separate table**: `flyway_schema_history_dummy`
- Does NOT pollute production migration history
- Idempotent: Safe to run multiple times

**Use cases:**
- Load test data in development environment
- Refresh sample data during development
- Populate staging with sample data
- **Never run in production**

**Check status:**
```bash
./gradlew flywayInfoDummy  # View dummy migration history
```

#### 2. **Migrate DDL + DML Only**
Deploy DDL and DML only (excludes dummy data):

```bash
./gradlew flywayMigrateDdlDml
```

**What it does:**
- Applies migrations from `db/migration/sql/ddl-dml/` folder only
- Records history in **production table**: `flyway_schema_history`
- Safe for production deployment
- Excludes all test/sample data

**Use cases:**
- Production deployments
- UAT/Staging deployments (production-like)
- Any environment where test data should NOT be loaded

**Check status:**
```bash
./gradlew flywayInfo  # View production migration history
```

#### 3. **View Dummy Migration Status**
Check the status of dummy migrations:

```bash
./gradlew flywayInfoDummy
```

**Shows:**
- Applied dummy migrations (version, description, timestamp)
- Pending dummy migrations
- Reads from `flyway_schema_history_dummy` table

#### 4. **Validate Dummy Migrations**
Verify dummy migration integrity:

```bash
./gradlew flywayValidateDummy
```

**Checks:**
- Checksum validation for applied dummy migrations
- Detects if dummy migration files were modified
- Reads from `flyway_schema_history_dummy` table

#### 5. **Clean Dummy History**
Reset dummy migration tracking (development only):

```bash
./gradlew flywayCleanDummy
```

**WARNING:** This drops the `flyway_schema_history_dummy` table only. It does NOT:
- Drop the main `flyway_schema_history` table
- Remove actual data from TBL_CLEAN_USER or other tables
- Affect production migration history

**Use when:**
- Resetting development environment
- Starting fresh with dummy migrations
- Cleaning up after testing

### Command Comparison

| Command | DDL/DML | Dummy | History Table | Production Safe |
|---------|---------|-------|---------------|-----------------|
| `flywayMigrate` | ✅ | ✅ | Both tables | ⚠️ No (includes dummy) |
| `flywayMigrateDummy` | ❌ | ✅ | `flyway_schema_history_dummy` | ❌ No (test data) |
| `flywayMigrateDdlDml` | ✅ | ❌ | `flyway_schema_history` | ✅ **Recommended** |
| `flywayInfo` | ✅ | ❌ | `flyway_schema_history` | ✅ Yes |
| `flywayInfoDummy` | ❌ | ✅ | `flyway_schema_history_dummy` | ℹ️ Dev only |

## Rollback System

### Date-Based Rollback Management

The project includes an automated rollback system that organizes rollback scripts by migration date and provides safe execution with validation.

### Creating Rollback Scripts

1. **Manual Creation**: Create rollback scripts in `src/main/resources/db/migration/sql/rollback/{YYMMDD}/`
2. **Auto-Creation**: The rollback task will auto-create the date folder if it doesn't exist

Rollback script naming follows the same convention:
```
V{YYMMDD}{XXX}__rollback_{original_description}.sql
```

Example:
```
rollback/260125/V260125001__rollback_create_initial_schema.sql
```

### Executing Rollbacks

Run the interactive rollback task:

```bash
./gradlew flywayRollback
```

The task will:
1. **Prompt for date** - Enter the migration date to rollback (format: YYMMDD)
2. **Auto-create folder** - Creates the rollback date directory if it doesn't exist
3. **Validate scripts** - Ensures rollback scripts exist for the specified date
4. **Confirm execution** - Shows which scripts will be executed and asks for confirmation
5. **Execute in sequence** - Runs rollback scripts in reverse order for proper rollback

### Rollback Safety Features

- ✅ **Date validation** - Ensures proper YYMMDD format
- ✅ **Script validation** - Confirms rollback scripts exist before execution
- ✅ **Confirmation prompt** - Requires explicit user confirmation
- ✅ **Sequential execution** - Automatically orders scripts for safe rollback
- ✅ **Auto folder creation** - Creates date directories as needed

### Example Rollback Session

```bash
$ ./gradlew flywayRollback

Enter the migration date to rollback (format: YYMMDD, e.g., 260125): 260125
This will execute rollback scripts: V260125002__rollback_insert_sample_data.sql, V260125001__rollback_create_initial_schema.sql
Are you sure? (yes/no): yes

Executing rollback for date: 260125
Rollback completed successfully for date: 260125
```

## Migration Best Practices

### 1. **DDL Guidelines**
- Always include `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`
- Create appropriate indexes for query performance
- Use meaningful table and column names
- Include migration header comments with purpose, author, and date
- Add audit fields (created_on, created_by, updated_on, updated_by)

### 2. **DML Guidelines**
- Keep reference data separate from schema changes
- Use transactions for bulk operations
- Include rollback considerations for data changes
- Test with realistic data volumes

### 3. **Rollback Guidelines**
- Create rollback scripts for every migration
- Test rollback scripts before deploying
- Consider data preservation vs. complete rollback
- Document rollback dependencies and ordering

### 4. **Version Control**
- Never modify existing migration files once applied
- Create new migrations for schema changes
- Use descriptive filenames and commit messages
- Tag releases with migration status

## Development Workflow

1. **Plan Migration** - Design schema changes and data updates
2. **Create Migration Files** - Write DDL/DML scripts with proper naming
3. **Create Rollback Scripts** - Write corresponding rollback scripts
4. **Test Locally** - Run migration and rollback on development database
5. **Validate** - Use `flywayValidate` to ensure integrity
6. **Commit** - Version control all migration files together
7. **Deploy** - Execute migrations in target environment

## Troubleshooting

### Common Issues

#### 1. **Migration Failed Partway**

**Symptoms:**
- Migration stopped with error
- Database in inconsistent state
- Schema history shows failed migration

**Solution:**
```bash
# Step 1: Check what failed
./gradlew flywayInfo

# Step 2: Repair the schema history (removes failed entry)
./gradlew flywayRepair

# Step 3: Fix the migration script if needed
# Edit the SQL file to fix the issue

# Step 4: Run migration again
./gradlew flywayMigrate
```

#### 2. **Checksum Validation Failed**

**Symptoms:**
```
ERROR: Validate failed: Migration checksum mismatch for migration version 260125001
```

**Cause:** Migration file was modified after it was executed

**Solution:**
```bash
# Option A: Repair checksums (if modification was intentional)
./gradlew flywayRepair

# Option B: Rollback and reapply (safer)
./gradlew flywayRollback  # Enter date to rollback
./gradlew flywayMigrate   # Apply corrected migration
```

#### 3. **Pending Migrations Not Executing**

**Check migration status:**
```bash
./gradlew flywayInfo
```

**Common causes:**
- Migration version lower than baseline version
- Migration file naming incorrect
- Migration file not in scanned locations

**Verify:**
```bash
./gradlew flywayValidate  # Shows validation errors
```

#### 4. **Rollback Failed**

**Symptoms:**
- Rollback task prompts for date but fails
- Rollback scripts not found

**Solutions:**

**Date Folder Not Found:**
```bash
# Run rollback - it auto-creates the folder
./gradlew flywayRollback
# Enter date: 260125
# Creates: src/main/resources/db/migration/sql/rollback/260125/
# Add rollback scripts and run again
```

**Rollback Scripts Missing:**
1. Create rollback scripts in `rollback/{YYMMDD}/` folder
2. Name: `V{YYMMDD}{XXX}__rollback_{description}.sql`
3. Run `./gradlew flywayRollback` again

#### 5. **Cannot Connect to Database**

**Check connection:**
```bash
# Test MariaDB connection
mysql -h localhost -u root -p clean_dev

# Verify gradle.properties settings
cat gradle.properties
```

**Override connection:**
```bash
./gradlew flywayInfo -Pflyway.url=jdbc:mariadb://localhost:3306/clean_dev
```

**Create database if it doesn't exist:**
```sql
mysql -u root -p
CREATE DATABASE clean_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 6. **Gradle Wrapper Not Found**

If you get `./gradlew: No such file or directory`:

```bash
# Generate Gradle wrapper
gradle wrapper --gradle-version 8.5

# Then try again
./gradlew flywayInfo
```

#### 7. **Dummy Migrations Appearing in Production History**

**Symptoms:**
- `flyway_schema_history` contains entries like `R__dev_sample_users`
- Production migration history polluted with test data entries

**Cause:** Used `flywayMigrate` instead of `flywayMigrateDdlDml`

**Prevention:**
Always use the correct command for each environment:
```bash
# Production/Staging - CORRECT
./gradlew flywayMigrateDdlDml

# Development - Load dummy separately
./gradlew flywayMigrateDdlDml  # First
./gradlew flywayMigrateDummy   # Then (uses separate history table)
```

**Cleanup if already polluted:**

**Option A: Repair (if only dummy entries are affected)**
```bash
# 1. Check what's in the history
./gradlew flywayInfo

# 2. Manually remove dummy migration entries
mysql -u root -p clean_dev
DELETE FROM flyway_schema_history WHERE description LIKE '%dummy%' OR description LIKE '%sample%';
exit

# 3. Verify cleanup
./gradlew flywayInfo
```

**Option B: Fresh start (development only)**
```bash
# 1. Backup data if needed
mysqldump -u root -p clean_dev > backup.sql

# 2. Drop and recreate schema
mysql -u root -p
DROP DATABASE clean_dev;
CREATE DATABASE clean_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit

# 3. Reapply migrations correctly
./gradlew flywayMigrateDdlDml  # Production migrations
./gradlew flywayMigrateDummy   # Dummy migrations (separate history)

# 4. Verify
./gradlew flywayInfo       # Should show only DDL/DML
./gradlew flywayInfoDummy  # Should show only dummy
```

#### 8. **Two History Tables - Which One to Check?**

**Question:** "I see both `flyway_schema_history` and `flyway_schema_history_dummy`. Which should I check?"

**Answer:**

**For production migration status:**
```bash
./gradlew flywayInfo  # Reads: flyway_schema_history
```

**For dummy/test data status:**
```bash
./gradlew flywayInfoDummy  # Reads: flyway_schema_history_dummy
```

**Verify table contents directly:**
```sql
-- Production migrations
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Dummy migrations
SELECT * FROM flyway_schema_history_dummy ORDER BY installed_rank;
```

**Expected separation:**
- `flyway_schema_history` - Contains only V{YYMMDD}XXX migrations from ddl-dml/
- `flyway_schema_history_dummy` - Contains only R__* migrations from dummy/

## SQL Best Practices

### Table Standards
- **Engine**: InnoDB
- **Charset**: utf8mb4
- **Collation**: utf8mb4_unicode_ci
- **Naming**: TBL_ prefix (e.g., TBL_USER, TBL_CONFIG)

### Required Fields
- **Primary Key**: `id BIGINT AUTO_INCREMENT PRIMARY KEY`
- **Audit Fields**:
  - `created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP`
  - `created_by VARCHAR(100)`
  - `updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
  - `updated_by VARCHAR(100)`

### Indexes
- Unique constraints for business keys (prefix: `uk_`)
- Indexes on frequently queried columns (prefix: `idx_`)
- Composite indexes for multi-column queries

### Comments
- Table comments describing purpose
- Column comments for clarity
- Migration file headers with metadata

### Example Migration Header
```sql
-- Migration: V260125001__create_initial_schema.sql
-- Description: Create initial database schema with user table
-- Author: Clean Architecture Project
-- Date: 2026-01-25
```

## Additional Resources

- [Flyway Documentation](https://documentation.red-gate.com/flyway)
- [Flyway Gradle Plugin](https://flywaydb.org/documentation/usage/gradle/)
- [MariaDB Documentation](https://mariadb.com/kb/en/documentation/)
- [Migration Naming Best Practices](https://www.red-gate.com/blog/database-devops/flyway-naming-patterns-matter)

## Support

For issues or questions:
1. Check this README for common solutions
2. Review Flyway documentation
3. Consult the sample project at `/Users/adzmien/Workspace/code/baseline-com.clean/clean-flyway-sql`
