# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run (dev profile)
./mvnw spring-boot:run

# Build (skip tests)
mvn clean package -DskipTests

# Run all tests (includes Testcontainers integration tests)
./mvnw verify

# Run specific test class
./mvnw test -Dtest="JwtServiceTest"
```

API available at `http://localhost:8080`, Swagger UI at `http://localhost:8080/swagger-ui.html`.

## Architecture

Spring Boot 4.x + Spring Security 7 (JWT RS256) + MySQL 8 + Flyway.

### Package Layout

All source lives under `com.uptask`:

| Package | Responsibility |
|---|---|
| `config/` | Security, CORS, JWT, async, OpenAPI, and typed config properties |
| `auth/` | Registration, login, refresh, password-reset flows |
| `security/` | JWT filter, `UserPrincipal`, RS256 signing service, auth error handlers |
| `token/` | `RefreshToken` entity + service (rotation + revocation on logout) |
| `otp/` | OTP generation, SHA-256 hashing, validation |
| `email/` | Async SMTP delivery via `TaskExecutor` + Thymeleaf templates |
| `audit/` | `AuditableEntity` base class (createdAt/updatedAt/createdBy/updatedBy via JPA) |
| `user/` | User entity, CRUD, DTOs, mapper |
| `role/` | Role entity and service |
| `category/` | Domain module example: entity, controller, service, mapper, DTOs, repo, exceptions |
| `admin/` | Role-gated admin endpoints |
| `common/` | Shared exceptions, response DTOs, `OtpUtil`, `HashUtil`, `SlugUtil`, constants |

### Request Flow

`Controller → Service → Repository` — no bypassing layers. DTOs are used at controller boundaries; entities are never exposed directly.

### Key Patterns

- **JWT**: Stateless RS256. Dev keys are pre-generated in `src/main/resources/keys/` (never use in production). Production keys must be mounted separately.
- **Refresh tokens**: Rotated on every use; revoked immediately on logout. Stored hashed.
- **OTPs**: Always stored SHA-256 hashed, never plaintext.
- **Account lockout**: Configurable failed-attempt threshold and lock duration via `SecurityProperties`.
- **Async email**: All email sending goes through Spring's `TaskExecutor` — never block the request thread.
- **Auditing**: Every entity extends `AuditableEntity`; `@EnableJpaAuditing` is active.

### Adding a New Domain Module

Follow the `category/` module as the reference pattern: entity → repository → service → mapper → DTOs → controller → exceptions. Register Flyway migrations under `src/main/resources/db/migration/` with sequential `V<n>__description.sql` naming.

## Configuration

`src/main/resources/application.yml` drives everything. Key environment variables:

| Var | Purpose |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Database connection |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP |
| `EMAIL_FROM`, `EMAIL_FROM_NAME` | Sender identity |
| `MAX_FAILED_LOGIN_ATTEMPTS` | Account lockout threshold |
| `ACCOUNT_LOCK_DURATION_MINUTES` | Lock duration |

CORS allowed origins default to `localhost:3000`, `4200`, and `8080`; override via `CorsProperties`.

## Database

Flyway manages schema via versioned migrations in `src/main/resources/db/migration/`. Current schema: users, roles, permissions, user_roles, role_permissions, refresh_tokens, otp_codes, login_attempts, categories. V11 seeds initial roles and permissions.
