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

Application defaults (`src/main/resources/application.properties`):
- DB: `jdbc:postgresql://localhost:5432/jib_pilot` (user/pass `postgres/postgres`)
- JWT secret: update `app.security.jwt.secret` to a strong value
- Default admin seed: set `app.admin.email` / `app.admin.password` before first run

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
