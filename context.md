# Application Context Snapshot

## Overview
- Spring Boot 3 (Java 21) REST API for students and courses.
- Persistence: PostgreSQL (prod), H2 (tests).
- Auth: JWT-based with Spring Security; roles `ADMIN` and `STUDENT`.
- Docs: Swagger UI with Bearer auth scheme configured.
- Packaging: Jib for container images; Docker Compose sets up Postgres.

## Domain
- Student: id, firstName, lastName, email (unique), courses (many-to-many), linked UserAccount.
- Course: id, code (unique), title, description, students (many-to-many).
- UserAccount: id, email (unique), password (BCrypt hash), role (ADMIN/STUDENT), optional linked Student (1:1).

## Key Endpoints
- Auth: `POST /api/v1/auth/register` (register student + user), `POST /api/v1/auth/login` (JWT).
- Students (secured, bearerAuth):
  - ADMIN: full CRUD, list all.
  - STUDENT: only own record (ownership enforced).
  - Enroll/drop course: `POST/DELETE /api/v1/students/{studentId}/courses/{courseId}` (ownership).
- Courses (secured, bearerAuth):
  - ADMIN: full CRUD, list students in course.
  - STUDENT: read/list courses.
- Swagger UI: `/swagger-ui.html` (Authorize with `Bearer <token>`).

## Security
- JWT properties: `app.security.jwt.secret` (must set strong value), `app.security.jwt.expiration-seconds`.
- Admin seed: set `app.admin.email` and `app.admin.password` before first run to create ADMIN user.
- Ownership helper: `SecurityGuard` used via `@PreAuthorize` to restrict student access to self unless ADMIN.

## Data/Config
- Default DB (app): `jdbc:postgresql://localhost:5432/jib_pilot`, user/pass `postgres/postgres` (update as needed).
- Test DB: H2 in-memory (`src/test/resources/application.properties`).
- Docker Compose: `docker-compose.yml` runs Postgres 16 with persisted volume.

## Build & Run
- Local run: `docker compose up -d` (DB), then `./gradlew bootRun`.
- Container run: `./gradlew jibDockerBuild` then `docker compose up -d` (uses app image `baicham/jib-pilot:latest`).
- Dev compose: `docker compose -f docker-compose.dev.yml up --build` (bind-mounts repo, runs `./gradlew bootRun` for hot reload; includes its own Postgres and a strong default JWT secret).
- Tests: `./gradlew test` (uses H2).
- Container image: `./gradlew jib` (push with Docker Hub creds/env vars).

## Notes
- Update `app.security.jwt.secret` and admin credentials for real environments.
- Email uniqueness enforced across Student and UserAccount.
- DTO naming: requests use `Create/Update...Request`, responses use `...Response`.
- Compose JWT secret is currently set to a strong random value; change it for your own deployment.
- Docker CLI not required in containers: `SPRING_DOCKER_COMPOSE_ENABLED` set to false in compose files to avoid Spring Boot trying to start Docker.
