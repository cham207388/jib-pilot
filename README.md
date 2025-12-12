# Jib Pilot – Students & Courses API

Spring Boot 3 application with student and course management, JWT auth (STUDENT/ADMIN roles), Postgres, Docker Compose, Jib, and Swagger UI.

## Prerequisites
- Java 21
- Docker + Docker Compose
- (Optional) PostgreSQL locally if not using Docker Compose

## Run locally
```bash
# Start Postgres
docker compose up -d

# Start the app
./gradlew bootRun
```

### Run everything with Docker Compose (no Dockerfile needed)
```bash
# Build image locally with Jib (to Docker daemon)
./gradlew jibDockerBuild

# Start app + Postgres
docker compose up -d
```

The compose file expects the image `baicham/jib-pilot:latest` (produced by `jibDockerBuild`) and will expose the app on `8080`. Override env vars (DB creds, JWT secret, admin creds) as needed.

### Dev container with hot reload (bootRun + bind mount)
```bash
# Run dev stack with source mounted and bootRun (includes Postgres)
docker compose -f docker-compose.dev.yml up --build
```
- Uses `Dockerfile.dev` to prime Gradle; mounts the repo into `/workspace` and runs `./gradlew bootRun` for hot reload.
- Set `APP_SECURITY_JWT_SECRET` in your shell (default in compose is a strong 64-byte hex).

> Note: Use one compose file per scenario: `docker-compose.dev.yml` for local dev build/run; `docker-compose.yml` with `jibDockerBuild` image for prod-like runs. Avoid `docker compose -f Dockerfile.dev up`.

Application defaults (`src/main/resources/application.properties`):
- DB: `jdbc:postgresql://localhost:5432/jib_pilot` (user/pass `postgres/postgres`)
- JWT secret: update `app.security.jwt.secret` to a strong value
- Default admin seed: set `app.admin.email` / `app.admin.password` before first run

### Generate a strong JWT secret
- Hex (64 bytes): `openssl rand -hex 64`
- Base64 (64 bytes): `openssl rand -base64 64`
- Python (URL-safe Base64, 64 bytes):
  ```bash
  python - <<'PY'
  import secrets, base64
  print(base64.urlsafe_b64encode(secrets.token_bytes(64)).decode())
  PY
  ```
Keep the secret out of git; set it via environment variables or a local `.env` (ignored by git). Rotating the secret invalidates existing tokens.

## Auth flow
- Register student: `POST /api/v1/auth/register` (firstName, lastName, email, password)
- Login: `POST /api/v1/auth/login` → returns JWT (`token` with `Bearer` scheme)
- Use JWT: add header `Authorization: Bearer <token>` for secured endpoints.
- Roles:
  - ADMIN: full access to students/courses
  - STUDENT: only own student record; can enroll/drop their courses

## APIs (high level)
- Students: CRUD, enroll/drop courses, list student courses; ownership enforced.
- Courses: CRUD, list enrollments (ADMIN only).
- Swagger UI: http://localhost:8080/swagger-ui.html (authorize with Bearer token).

## Running tests
```bash
./gradlew test
```

## Build & containerize
```bash
# Build jar and container image without pushing
./gradlew jib

# Build and push to Docker Hub (requires credentials or env vars)
./gradlew jib -PdockerHubUsername=baicham -PdockerHubPassword=your_password
# or set DOCKER_HUB_USERNAME / DOCKER_HUB_PASSWORD env vars
```
