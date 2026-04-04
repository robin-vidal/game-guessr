# auth-service

Authentication microservice for **Game Guessr** — manages user registration, login, and JWT token validation.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4 |
| Security | Spring Security + JWT (jjwt 0.12) |
| Database | PostgreSQL 16 (prod/local) · H2 (dev/test) |
| API Docs | Springdoc OpenAPI 3 (Swagger UI) |
| Build | Maven 3.9 |

---

## Prerequisites

- **Java 21** (Corretto or Temurin recommended)
- **Maven 3.9+**
- **Docker + Docker Compose**

> ⚠️ If your `mvn --version` shows Java 25 (Homebrew default), add this to your `~/.zshrc`:
> ```bash
> export JAVA_HOME=$(/usr/libexec/java_home -v 21)
> export PATH="$JAVA_HOME/bin:$PATH"
> ```
> Then `source ~/.zshrc`.

---

## Running Locally (recommended)

This runs **PostgreSQL in Docker** with the **service on your host** (fast iteration, no rebuild needed).

### 1. Start infrastructure

```bash
# From the project root (game-guessr/)
docker compose up -d postgres
```

Or use the full stack:
```bash
docker compose up -d
```

Wait ~10 seconds for PostgreSQL to be ready.

### 2. Run the service

```bash
cd backend/auth-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 3. Open Swagger UI

→ **http://localhost:8081/swagger-ui.html**

---

## Running Tests

```bash
mvn test
# → Tests run: 50, Failures: 0, Errors: 0
```

---

## Running Everything in Docker

Builds and runs the service container alongside infra:

```bash
# From project root
docker compose --profile auth up --build
```

Service will be available at **http://localhost:8081/swagger-ui.html**

---

## Spring Profiles

| Profile | Database | Use case |
|---|---|---|
| `dev` | H2 (in-memory) | **Quick local run**, no Docker needed |
| `local` | PostgreSQL (Docker) | **Normal development** |
| `docker` | PostgreSQL (container) | Full Docker Compose stack |

---

## API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Authenticate and receive JWT token |
| `POST` | `/api/auth/logout` | Invalidate JWT token (requires `Authorization: Bearer <token>`) |
| `GET` | `/api/auth/me` | Get current user info from JWT token (requires `Authorization: Bearer <token>`) |

### Register

```json
POST /api/auth/register
{
  "username": "player1",
  "password": "securepassword123"
}

Response 201:
{
  "id": "uuid",
  "username": "player1"
}
```

### Login

```json
POST /api/auth/login
{
  "username": "player1",
  "password": "securepassword123"
}

Response 200:
{
  "id": "uuid",
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Logout

```
POST /api/auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Response 204 No Content
```

### Me

```
GET /api/auth/me
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...

Response 200:
{
  "userId": "uuid",
  "username": "player1"
}
```

Full documentation: **http://localhost:8081/swagger-ui.html**

---

## Architecture

### Hexagonal (Ports & Adapters)

```
src/main/java/com/gameguessr/auth/
├── domain/                  ← Pure business logic (no framework imports)
│   ├── model/               User, JwtTokenInfo, LoginResult
│   └── port/
│       ├── inbound/         AuthUseCase, JwkUseCase (interfaces)
│       └── outbound/        UserRepository, TokenService, TokenBlacklist (interfaces)
├── application/             ← Use case orchestration + REST adapters
│   ├── service/             AuthApplicationService, JwkApplicationService
│   └── rest/                AuthController, JwkController, GlobalExceptionHandler, DTOs
└── infrastructure/          ← Framework adapters
    ├── persistence/          UserEntity, UserJpaRepository, UserRepositoryAdapter
    ├── config/               SecurityConfig, JwtProperties, OpenApiConfig
    └── security/              JwtTokenService, JwtAuthenticationFilter, UserDetailsServiceImpl, InMemoryTokenBlacklist
```

### Authentication Flow

```
┌─────────────┐     POST /register      ┌─────────────────┐
│   Client    │ ───────────────────────▶│  AuthController │
│             │                          └────────┬────────┘
│             │                                   │
│             │     POST /login                   ▼
│             │ ───────────────────────┐  ┌────────────────────┐
│             │                        │  │ AuthApplication    │
│             │                        │  │ Service            │
│             │                        │  └────────┬───────────┘
│             │                        │           │
│             │     JWT Token          │           ▼
│             │ ◀──────────────────────│  ┌────────────────────┐
│             │                        │  │ UserRepository     │
│             │                        │  │ (PostgreSQL)      │
│             │                        │  └────────────────────┘
│             │                        │
│             │     GET /protected     │
│             │ ───────────────────────│
│             │ Authorization: Bearer  │
│             │                        │
│             │     200 OK             │
│             │ ◀──────────────────────│
└─────────────┘                        │
                                       ▼
                              ┌────────────────────┐
                              │ JwtAuthentication  │
                              │ Filter             │
                              └────────┬───────────┘
                                       │
                                       ▼
                              ┌────────────────────┐
                              │ TokenBlacklist     │
                              │ (in-memory)        │
                              └────────────────────┘
```

---

## Configuration

### JWT Settings

| Property | Description | Default |
|---|---|---|
| `jwt.rsa-private-key` | RSA private key (PEM) | **Required** - no default |
| `jwt.rsa-public-key` | RSA public key (PEM) | **Required** - no default |
| `jwt.expiration` | Token validity (ms) | `86400000` (24h) |

⚠️ **Production**: Set these via environment variables:
```bash
export JWT_RSA_PRIVATE_KEY="$(cat /path/to/private.pem)"
export JWT_RSA_PUBLIC_KEY="$(cat /path/to/public.pem)"
export JWT_EXPIRATION="86400000"
```

### JWKS Endpoint

The service exposes a public key in JWKS format at:
```
GET /.well-known/jwks.json
```

This allows other services to verify JWT signatures without sharing the private key.

---

## Docker

Build the image:
```bash
cd backend/auth-service
docker build -t gameguessr/auth-service:latest .
```

Run:
```bash
docker run -p 8081:8081 \
  -e JWT_RSA_PRIVATE_KEY="$(cat private.pem)" \
  -e JWT_RSA_PUBLIC_KEY="$(cat public.pem)" \
  -e DB_HOST=postgres \
  -e DB_NAME=authdb \
  -e DB_USER=authuser \
  -e DB_PASSWORD=authpass \
  gameguessr/auth-service:latest
```