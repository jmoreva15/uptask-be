# Auth API

Production-ready authentication and authorization REST API built with Spring Boot 4, Java 21, Spring Security, JWT (RS256), MySQL, and Flyway.

---

## Table of Contents
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Running the App](#running-the-app)
- [Environment Variables](#environment-variables)
- [RSA Keys](#rsa-keys)
- [API Endpoints](#api-endpoints)
- [curl Examples](#curl-examples)
- [Running Tests](#running-tests)
- [Production Checklist](#production-checklist)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21, Spring Boot 4 |
| Security | Spring Security 7, JWT RS256 (JJWT 0.12) |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Migrations | Flyway 10 |
| Email | Spring Mail + Thymeleaf templates |
| Docs | springdoc-openapi (Swagger UI) |
| Mapping | MapStruct |
| Tests | JUnit 5, Mockito, Testcontainers |

---

## Architecture

```
src/main/java/com/company/auth
├── config/          # SecurityConfig, JwtConfig, CorsConfig, OpenApiConfig, AsyncConfig
├── auth/            # Registration, login, token refresh, password reset flows
├── user/            # User entity, repository, service, controller
├── role/            # Role entity and service
├── permission/      # Permission entity
├── token/           # RefreshToken entity and service (rotation + revocation)
├── otp/             # OTP generation, storage (hashed), and validation
├── email/           # Async email sending with Thymeleaf templates
├── audit/           # JPA auditing (createdAt, updatedAt, createdBy, updatedBy)
├── security/        # JWT service (RS256), filter, UserPrincipal, error handlers
├── common/          # Exceptions, DTOs (ErrorDto, PageDto), utilities, constants
└── admin/           # Admin endpoints with role-based access
```

**Key design decisions:**
- **Stateless**: JWT access tokens, no server-side session.
- **RS256**: Private key signs tokens; public key validates them — public key can be shared with any service.
- **OTP hashed**: OTPs are stored as SHA-256 hashes, never plain text.
- **Refresh token rotation**: Each refresh call invalidates the old token and issues a new one.
- **Account lockout**: After N failed attempts, account is locked for a configurable duration.
- **Secrets via env**: Production keys, DB credentials, and mail config come from environment variables only.

---

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker (for MySQL or Testcontainers)
- MySQL 8.0+ (or use the Docker command below)

### Start MySQL with Docker
```bash
docker run -d \
  --name auth-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=auth_db \
  -p 3306:3306 \
  mysql:8.0
```

### Clone and build
```bash
git clone <repo-url>
cd auth
mvn clean package -DskipTests
```

---

## Running the App

```bash
# Dev profile (default)
./mvnw spring-boot:run

# Explicit profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API will start on `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Environment Variables

All secrets for **production** must be provided via environment variables. Never commit secrets to source control.

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL, e.g. `jdbc:mysql://host:3306/auth_db` |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `MAIL_HOST` | SMTP host |
| `MAIL_PORT` | SMTP port |
| `MAIL_USERNAME` | SMTP username |
| `MAIL_PASSWORD` | SMTP password |
| `JWT_PRIVATE_KEY_LOCATION` | Path to RSA private key, e.g. `file:/secrets/private.pem` |
| `JWT_PUBLIC_KEY_LOCATION` | Path to RSA public key, e.g. `file:/secrets/public.pem` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated origins |
| `EMAIL_FROM` | Sender email address |
| `EMAIL_FROM_NAME` | Sender display name |

---

## RSA Keys

### Development
Development keys are pre-generated and bundled in `src/main/resources/keys/`:
- `dev-private.pem` — PKCS8 RSA private key
- `dev-public.pem` — X.509 RSA public key

**These keys must never be used in production.**

### Generate new keys
```bash
# Generate RSA-2048 private key (PKCS1)
openssl genrsa -out private-pkcs1.pem 2048

# Convert to PKCS8 (required by Java)
openssl pkcs8 -topk8 -inform PEM -in private-pkcs1.pem -out private.pem -nocrypt

# Extract public key (X.509)
openssl rsa -in private-pkcs1.pem -pubout -out public.pem

# Remove intermediate file
rm private-pkcs1.pem
```

### Production
In production, mount keys as a volume or load from a secret manager:
```yaml
jwt:
  private-key-location: file:/run/secrets/jwt-private.pem
  public-key-location: file:/run/secrets/jwt-public.pem
```

---

## API Endpoints

### Authentication (`/api/v1/auth`)

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/register` | Register new user | Public |
| POST | `/activate` | Activate account with OTP | Public |
| POST | `/resend-activation` | Resend activation OTP | Public |
| POST | `/login` | Login, receive tokens | Public |
| POST | `/refresh` | Rotate refresh token | Public |
| POST | `/logout` | Revoke refresh token | Public |
| POST | `/forgot-password` | Request password reset OTP | Public |
| POST | `/verify-password-reset-otp` | Validate OTP, get reset token | Public |
| POST | `/reset-password` | Set new password via reset token | Public |
| POST | `/change-password` | Change password (authenticated) | Bearer |

### Profile (`/api/v1/me`)

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/` | Get current user profile | Bearer |

### Admin (`/api/v1/admin`)

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/users` | List users with pagination | ADMIN/SUPER_ADMIN |
| GET | `/users/{id}` | Get user by ID | ADMIN/SUPER_ADMIN |
| PATCH | `/users/{id}/status` | Update user status | SUPER_ADMIN |

---

## curl Examples

### Register
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass1!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Activate account
```bash
curl -X POST http://localhost:8080/api/v1/auth/activate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "otp": "123456"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass1!"
  }'
```

### Refresh token
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<your-refresh-token>"}'
```

### Logout
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<your-refresh-token>"}'
```

### Forgot password
```bash
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com"}'
```

### Verify reset OTP
```bash
curl -X POST http://localhost:8080/api/v1/auth/verify-password-reset-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "otp": "654321"}'
```

### Reset password
```bash
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "resetToken": "<reset-token-from-previous-step>",
    "newPassword": "NewSecurePass1!",
    "confirmPassword": "NewSecurePass1!"
  }'
```

### Get profile
```bash
curl http://localhost:8080/api/v1/me \
  -H "Authorization: Bearer <your-access-token>"
```

### Change password
```bash
curl -X POST http://localhost:8080/api/v1/auth/change-password \
  -H "Authorization: Bearer <your-access-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "OldPass1!",
    "newPassword": "NewPass2!",
    "confirmPassword": "NewPass2!"
  }'
```

### Admin — list users
```bash
curl "http://localhost:8080/api/v1/admin/users?page=0&size=20" \
  -H "Authorization: Bearer <admin-access-token>"
```

---

## Running Tests

### Unit tests only
```bash
./mvnw test -Dtest="JwtServiceTest,AuthServiceTest"
```

### All tests (requires Docker for Testcontainers)
```bash
./mvnw verify
```

Testcontainers will automatically spin up a MySQL 8.0 container for integration tests.

---

## Production Checklist

- [ ] Replace dev RSA keys with production keys (mounted as volume or from secret manager)
- [ ] Set all environment variables (see table above)
- [ ] Use `spring.profiles.active=prod`
- [ ] Configure SMTP with TLS
- [ ] Set `CORS_ALLOWED_ORIGINS` to your actual frontend domain
- [ ] Use a connection pool (HikariCP config is included in `application-prod.yml`)
- [ ] Set up log aggregation (Datadog, ELK, CloudWatch)
- [ ] Enable HTTPS (TLS termination at load balancer or configure Spring)
- [ ] Review `MAX_FAILED_LOGIN_ATTEMPTS` and `ACCOUNT_LOCK_DURATION_MINUTES` for your threat model
- [ ] Schedule a job to clean up expired refresh tokens and OTPs
