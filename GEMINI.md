# Jib Pilot Project Overview for Gemini

This document provides a comprehensive overview of the Jib Pilot project, designed to serve as instructional context for AI agents like Gemini. It covers the project's purpose, technology stack, development practices, and operational guidelines.

## Project Overview

**Jib Pilot** is a modern, production-ready Spring Boot REST API application for managing students and courses. It features robust JWT-based authentication, fine-grained role-based access control, and advanced full-text search capabilities using PostgreSQL's `pg_textsearch` extension. The application is designed for containerized deployment using Docker and Jib, ensuring portability and ease of deployment.

**Key Features:**
*   **Student & Course Management:** Full CRUD operations for students and courses.
*   **Enrollment System:** Students can enroll in and drop courses.
*   **Full-Text Search:** BM25-ranked search for courses and students using `pg_textsearch`.
*   **JWT Authentication & RBAC:** Secure token-based authentication with ADMIN and STUDENT roles.
*   **API Documentation:** Interactive Swagger UI for API exploration.
*   **Actuator Endpoints:** Health checks, metrics, and custom monitoring endpoints.
*   **Containerization:** Docker, Docker Compose, and Jib for streamlined deployment.
*   **Database Migrations:** Automatic schema management with Hibernate.
*   **Rate Limiting:** Tiered rate limiting using Bucket4j to protect API endpoints.
*   **Comprehensive Testing:** Unit and integration tests with H2 in-memory database.
*   **APM Integration:** New Relic Java agent integrated for application performance monitoring.

## Technology Stack

*   **Framework:** Spring Boot 4.0.0
*   **Language:** Java 21
*   **Build Tool:** Gradle
*   **Database:** PostgreSQL 17 (production), H2 (testing)
*   **Search:** `pg_textsearch` extension for BM25-ranked full-text search
*   **Security:** Spring Security with JWT (JSON Web Tokens)
*   **API Documentation:** SpringDoc OpenAPI (Swagger UI)
*   **Monitoring:** Spring Boot Actuator, New Relic APM
*   **Containerization:** Docker, Docker Compose, Jib
*   **ORM:** Spring Data JPA with Hibernate
*   **Validation:** Jakarta Validation
*   **Utilities:** Lombok, Jackson
*   **Rate Limiting:** Bucket4j

## Building and Running

### Prerequisites

*   Java 21 or higher
*   Docker and Docker Compose
*   PostgreSQL 17 or 18 (for `pg_textsearch` features)
*   Gradle (wrapper included)

### Local Development

1.  **Start PostgreSQL with Docker Compose:**
    ```bash
    docker compose up -d
    ```
2.  **Run the application:**
    ```bash
    ./gradlew bootRun
    ```
    The application will be available at `http://localhost:8085`.

### Development with Hot Reload

For live reloading during development:
```bash
docker compose -f compose.yml up --build
```

### Development with Full-Text Search (pg_textsearch)

To enable BM25-ranked full-text search:
```bash
make up-search
# or
docker compose -f docker-compose-pg-search.yml up --build
```

### Production-like Container Deployment

1.  **Build the container image:**
    ```bash
    ./gradlew jibDockerBuild
    ```
2.  **Start the application and database:**
    ```bash
    docker compose up -d
    ```
    The application will be exposed on port `8080`.

### Building JAR

```bash
./gradlew build
```
The JAR file will be located in `build/libs/jib-pilot-0.0.1.jar`.

## Testing

### Run All Tests

```bash
./gradlew test
```

### Conditional Test Execution

*   **Run only WebMvcTest tests:**
    ```bash
    ./gradlew webmvcTest
    ```
    (Tests tagged with `@Tag("webmvc")`, loading only the web layer)
*   **Run only SpringBootTest integration tests:**
    ```bash
    ./gradlew springBootTest
    ```
    (Tests tagged with `@Tag("spring-boot")`, loading the full application context)

### Test Reports

HTML test reports are generated in `build/reports/tests/test/index.html` (and similar paths for specific test suites).

## Development Conventions

### Project Structure

The project follows a standard Spring Boot application structure, with clear separation of concerns:
*   `src/main/java/com/abc/jibpilot/`: Root package for application logic.
    *   `actuator/`: Custom Actuator endpoints.
    *   `auth/`: Authentication and authorization components (controllers, services, filters, entities).
    *   `config/`: Spring configuration classes.
    *   `course/`: Course management modules.
    *   `ratelimit/`: Rate limiting implementation.
    *   `security/`: Security utilities.
    *   `student/`: Student management modules.
*   `src/main/resources/`: Application configuration (`application.yml`), database migrations, static resources.
*   `src/test/`: Unit and integration tests.

### Configuration

*   Application configuration is managed via `application.yml`.
*   Sensitive configurations (e.g., JWT secret, database credentials) should be provided via environment variables.
*   New Relic APM is integrated and configured via `src/main/newrelic/newrelic.yml` and environment variables (`NEW_RELIC_LICENSE_KEY`, `NEW_RELIC_APP_NAME`).

### API Documentation

Interactive API documentation is available via Swagger UI at `http://localhost:8085/swagger-ui.html` when the application is running.

### Authentication & Authorization

*   JWT-based authentication is used. Tokens must be sent in the `Authorization: Bearer <token>` header.
*   Two roles are defined: `ADMIN` (full access) and `STUDENT` (limited to own data and course viewing).
*   Method-level security (`@PreAuthorize`) and a `SecurityGuard` component enforce access control.

### Rate Limiting

*   Implemented using Bucket4j with tiered limits for authentication, public, and authenticated endpoints.
*   Limits are configurable in `application.yml`.
*   Responses include `X-RateLimit-*` headers.

### Commit Message Format

Commit messages are enforced via a Git hook (`.githooks/commit-msg`) and must adhere to one of the following formats:
*   `ABC-####: custom message` (e.g., `ABC-1234: login page`)
*   `Love-#### custom message` (4 to 9 digits, case-insensitive)

## Troubleshooting

*   **Database Connection Issues:** Verify PostgreSQL is running and credentials in `application.yml` are correct.
*   **JWT Token Issues:** Check JWT secret, token expiration, and correct `Authorization` header format.
*   **Port Conflicts:** Default application port is `8085`. Check `application.yml` or Docker Compose configurations.
*   **Rate Limiting:** Ensure `app.rate-limiting.enabled` is `true` and check logs for errors.

This `GEMINI.md` provides a solid foundation for understanding and interacting with the Jib Pilot project.
