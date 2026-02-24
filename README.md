# CLAUDE.md — com-clean-fresh

> Authoritative instructions for Claude Code. Follow these strictly in every session.

---

## 1. Project Overview

Multi-module microservices monorepo. Currently building the baseline — reusable structure and code — so new API services can be scaffolded quickly with consistent patterns.

| Module | Type | Purpose |
|---|---|---|
| `clean-common-lib` | java-library (mavenLocal) | Shared base classes, DTOs, entities, mappers, services, controllers, filters, auto-configuration, utilities |
| `clean-backoffice-api` | Spring Boot app | First/reference API microservice; depends on `clean-common-lib:0.0.1-SNAPSHOT` |
| `clean-common-sql` | Resources | Flyway SQL migrations — DDL/DML + environment-specific dummy data |
| `clean-common-k8s` | Manifests | Kubernetes manifests with Kustomize overlays, deploy scripts, env configs |
| `clean-common-doc` | Documentation | Onboarding checklists, guides, code review reports |

**Tech Stack:** Java 21 (Temurin), Spring Boot 3.5.10, Gradle 8.8 (Groovy DSL), MariaDB, Redis, Infinispan, MapStruct 1.6.3, Lombok 1.18.36, Log4j2

---

## 2. Environment & Build

- **Always run `j21` before any Gradle command** — sets Java 21 runtime (Temurin-21.0.9)
- Each module has its own `gradlew` — **there is no root-level wrapper**
- `clean-common-lib` must be published to mavenLocal before building any API module

### Build Commands (per module)
```bash
# Build clean-common-lib first
j21 && /Users/adzmien/Workspace/code/github/com-clean-fresh/clean-common-lib/gradlew -p /Users/adzmien/Workspace/code/github/com-clean-fresh/clean-common-lib clean build publishToMavenLocal

# Then build API module
j21 && /Users/adzmien/Workspace/code/github/com-clean-fresh/clean-backoffice-api/gradlew -p /Users/adzmien/Workspace/code/github/com-clean-fresh/clean-backoffice-api clean build

# Run tests (per module)
j21 && /Users/adzmien/Workspace/code/github/com-clean-fresh/clean-common-lib/gradlew -p /Users/adzmien/Workspace/code/github/com-clean-fresh/clean-common-lib test
```

---

## 3. Design Principles — MANDATORY

### SOLID (Strictly Enforced)
- **SRP**: Each class has one reason to change
- **OCP**: Abstract base classes open for extension via generics — never modify base classes for domain-specific logic
- **LSP**: Subtypes are substitutable (e.g. `BaseJpaCrudService` extends `BaseJpaReadService`)
- **ISP**: `ReadService`/`CrudService` segregation; `ReadMapper`/`WriteMapper`/`BaseMapper` hierarchy
- **DIP**: Controllers depend on interfaces (`ReadService`, `CrudService`), never concrete implementations

### DRY
- All reusable code lives in `clean-common-lib` — never duplicate cross-service logic in API modules
- New API modules extend base classes, not copy-paste

### KISS
- Zero-override controllers via generics — minimal configuration for new domains
- Don't over-engineer; add complexity only when clearly justified

### GoF Design Patterns — Use When Best Approach
Patterns already in use (follow these as reference):
- **Template Method**: `BaseJpaCrudService`, `BaseJpaReadService`, `BaseCrudController`, `BaseReadController`
- **Strategy**: `FilterStrategy` → `ExactFilterStrategy`, `LikeFilterStrategy`
- **Decorator**: Service interfaces enable wrapping for caching, logging, authorization
- **Factory**: `ConfigCacheEntryFactory`
- **Facade**: `ConfigCacheFacadeService`
- **Builder**: Lombok `@SuperBuilder` throughout entity/DTO hierarchy

Apply additional GoF patterns when they genuinely simplify or improve the design. Do not force patterns where a simpler solution suffices.

---

## 4. Coding Conventions

### Entity
- Extends `BaseEntity` (`@MappedSuperclass` with audit fields + soft-delete)
- Annotations: `@Entity`, `@Table(name = "tbl_clean_{name}")`, `@SuperBuilder`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@Getter`, `@Setter`
- Soft-delete: `@SQLRestriction("deleted = false")` — inherited from `BaseEntity`
- Use `@SuperBuilder` (NOT `@Builder`) for entity inheritance

### DTO
- Extends `BaseDTO` (or a domain-specific base DTO that extends `BaseDTO`)
- Annotations: `@SuperBuilder`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@Getter`, `@Setter`, `@JsonInclude(JsonInclude.Include.NON_NULL)`
- Implements `Serializable`

### Mapper
- Extends `BaseMapper<E, D>` (which combines `ReadMapper` + `WriteMapper`)
- Annotation: `@Mapper(componentModel = "spring")`
- For read-only domains, use `ReadMapper<E, D>` only

### Repository
- Extends `BaseJpaRepository<E, ID>`

### Service
- CRUD: extends `BaseJpaCrudService<E, ID, D>`
- Read-only: extends `BaseJpaReadService<E, ID, D>`
- Class-level `@Transactional(readOnly = true)`; write methods annotated with `@Transactional`
- Annotated with `@Service`

### Controller
- CRUD: extends `BaseCrudController<ID, D, F>`
- Read-only: extends `BaseReadController<ID, D, F>`
- `@RestController` + `@RequestMapping("api/v1/{domain}")`

### Build Configuration
- Annotation processor order in `build.gradle`: **Lombok BEFORE MapStruct**, then `spring-context`
- MapStruct compiler arg: `-Amapstruct.defaultComponentModel=spring`
- API modules: exclude Logback globally (`configurations.all { exclude ... spring-boot-starter-logging }`); use `spring-boot-starter-log4j2` + LMAX Disruptor
- `clean-common-lib`: `jakarta.persistence-api` must be explicit `api` dependency (does NOT resolve transitively)

---

## 5. New Microservice Onboarding

When creating a new API module:
1. Copy `clean-backoffice-api` structure as template
2. Update `build.gradle`: group, description, module-specific dependencies
3. Create domain package `com.clean.{servicename}` with sub-packages: `entity`, `dto`, `mapper`, `repository`, `service`, `controller`
4. Each entity extends `BaseEntity`, DTO extends `BaseDTO`, mapper extends `BaseMapper`, repository extends `BaseJpaRepository`, service extends `BaseJpaCrudService`, controller extends `BaseCrudController`
5. Add SQL migrations in `clean-common-sql/src/main/resources/db/migration/sql/ddl-dml/`
6. Add K8s manifests in `clean-common-k8s/com-clean-dev/` if needed
7. Migration naming: `V{YYMMDD}{seq}__description.sql` (e.g. `V260125001__create_tbl_clean_user.sql`)

---

## 6. Code Quality & Review

- Every commit must follow SOLID/DRY/KISS principles
- Apply GoF patterns when they are genuinely the best approach
- Always implement best practices: null safety, proper exception handling, immutability where possible
- **Code review reports**: Generated as Markdown files in `clean-common-doc/feature/review/`
- **Review rating scale**: 1–10

---

## 7. Key File Paths

### clean-common-lib (base package: `com.clean.common`)
| Layer | Files |
|---|---|
| Entity | `entity/BaseEntity.java` |
| DTOs | `base/dto/BaseDTO.java`, `BaseResponseDTO.java`, `BaseRequestDTO.java`, `PageDTO.java`, `PageRequestDTO.java`, `ProjectedRequestDTO.java` |
| Mappers | `mapper/ReadMapper.java`, `WriteMapper.java`, `BaseMapper.java` |
| Services | `base/service/ReadService.java`, `CrudService.java`, `BaseJpaReadService.java`, `BaseJpaCrudService.java` |
| Controllers | `base/controller/BaseReadController.java`, `BaseCrudController.java` |
| Repository | `base/repository/BaseJpaRepository.java` |
| Query/Filter | `base/query/QueryBuilder.java`, `DefaultEntityQuery.java`, `strategy/FilterStrategy.java`, `ExactFilterStrategy.java`, `LikeFilterStrategy.java` |
| Filters | `filter/ServerTraceFilter.java`, `RequestResponseLoggingFilter.java`, `TraceResponseAdvice.java` |
| Auto-config | `autoconfigure/CleanCommonAutoConfiguration.java`, `CleanCacheAutoConfiguration.java`, `CleanConfigCacheConsumerAutoConfiguration.java` |
| Constants | `constant/ErrorCode.java`, `QueryMode.java`, `TransactionType.java`, `TransactionCode.java` |
| Utilities | `util/PageableUtil.java`, `FilterSpecificationUtil.java`, `QueryBuilderUtil.java`, `JsonUtil.java` |
| Exception | `exception/GlobalExceptionHandler.java`, `ColumnValidationException.java` |

### clean-backoffice-api (base package: `com.clean.backoffice`) — Reference Implementation
- Entity: `entity/ConfigEntity.java`
- DTOs: `dto/ConfigDTO.java`, `ConfigFilterDTO.java`, `ConfigValueResponseDTO.java`, `ConfigCacheRefreshResultDTO.java`
- Mapper: `mapper/ConfigMapper.java`, `ConfigCacheMapper.java`
- Repository: `repository/ConfigRepository.java`
- Service: `service/ConfigService.java`
- Controller: `controller/ConfigController.java`
- Cache: `cache/ConfigCachePolicy.java`, `ConfigCacheLoader.java`, `ConfigCacheStore.java`, `ConfigCacheRefreshService.java`, `ConfigCacheEntryFactory.java`, `ConfigActiveValueResolver.java`
- Facade: `facade/ConfigCacheFacadeService.java`

---

## 8. Best Practices Checklist

- [ ] Use interface-based dependency injection (DIP)
- [ ] Segregate read/write interfaces (ISP)
- [ ] Extend base classes rather than duplicating code (DRY)
- [ ] Use `@SuperBuilder` for all entity/DTO inheritance
- [ ] Soft-delete by default (never hard-delete unless explicitly required)
- [ ] Class-level `@Transactional(readOnly = true)`, method-level `@Transactional` for writes
- [ ] Keep controllers thin — delegate to services
- [ ] Filter DTOs for criteria queries, not query parameters
- [ ] All REST responses wrapped in `BaseResponseDTO`

---

## 9. Future Automation (Planned)

Claude Code skills, agents, hooks, and commands will be set up for:
- Automated code review on every commit (rated 1–10, reports to `clean-common-doc/feature/review/`)
- New microservice scaffolding
- Test generation
- Deployment automation
- All Claude Code automation features (skills, agents, hooks, commands) will be fully utilized
