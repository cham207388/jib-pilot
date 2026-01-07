# Jib Pilot – Students & Courses Management API

A comprehensive Spring Boot REST API application for managing students and courses with JWT-based authentication, role-based access control, and containerized deployment support.

## Overview

Jib Pilot is a modern, production-ready Spring Boot application that provides a complete solution for student and course management. It features secure authentication, role-based authorization, comprehensive API documentation, and monitoring capabilities through Spring Boot Actuator.

## Features

- **Student Management**: Full CRUD operations for student records
- **Course Management**: Create, read, update, and delete courses
- **Enrollment System**: Students can enroll in and drop courses
- **JWT Authentication**: Secure token-based authentication with configurable expiration
- **Role-Based Access Control**: Two roles (ADMIN and STUDENT) with different permission levels
- **API Documentation**: Interactive Swagger UI for API exploration
- **Actuator Endpoints**: Health checks, metrics, and custom monitoring endpoints
- **Container Support**: Docker and Docker Compose configurations for easy deployment
- **Database Migrations**: Flyway-managed schema migrations that run automatically on startup
- **Code Quality**: SonarQube integration for continuous code analysis
- **Comprehensive Testing**: Unit and integration tests with H2 in-memory database

## Technology Stack

- **Framework**: Spring Boot 4.0.0
- **Language**: Java 21
- **Build Tool**: Gradle
- **Database**: PostgreSQL (production), H2 (testing)
- **Security**: Spring Security with JWT (JSON Web Tokens)
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Monitoring**: Spring Boot Actuator
- **Containerization**: Docker, Docker Compose, Jib
- **ORM**: Spring Data JPA with Hibernate
- **Validation**: Jakarta Validation
- **Utilities**: Lombok, Jackson

## Prerequisites

- **Java 21** or higher
- **Docker** and **Docker Compose** (for containerized deployment)
- **PostgreSQL** (optional, if not using Docker Compose)
- **Gradle** (optional, wrapper included)

## Project Structure

<details>
<summary>Project Structure</summary>

```txt
jib-pilot/
├── src/
│   ├── main/
│   │   ├── java/com/abc/jibpilot/
│   │   │   ├── actuator/          # Custom actuator endpoints
│   │   │   ├── auth/              # Authentication & authorization
│   │   │   │   ├── controller/    # Auth endpoints
│   │   │   │   ├── dto/           # Data transfer objects
│   │   │   │   ├── entity/        # UserAccount entity
│   │   │   │   ├── filter/       # JWT authentication filter
│   │   │   │   ├── model/        # Security models (Role, AppUserDetails)
│   │   │   │   ├── repository/   # User repository
│   │   │   │   └── service/      # Auth services
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── course/           # Course management
│   │   │   ├── security/         # Security utilities
│   │   │   └── student/          # Student management
│   │   └── resources/
│   │       ├── application.yml   # Application configuration
│   │       ├── db/migration/     # Flyway migration files
│   │       └── static/           # Static resources
│   └── test/                     # Test classes
├── build.gradle                   # Build configuration
├── docker-compose.yml            # Production Docker Compose
├── docker-compose.dev.yml        # Development Docker Compose
└── Dockerfile.dev                # Development Dockerfile
```

</details>

## Configuration

### Application Properties

The application uses `application.yml` for configuration. Key settings:

```yaml
server:
  port: 8085

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/jib_pilot
    username: postgres
    password: postgres

app:
  security:
    jwt:
      secret: <your-secret-here>
      expiration-seconds: 3600
  admin:
    email: admin@example.com
    password: ChangeMe123!

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

### Environment Variables

You can override configuration using environment variables:

- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `APP_SECURITY_JWT_SECRET`: JWT signing secret
- `APP_ADMIN_EMAIL`: Admin user email
- `APP_ADMIN_PASSWORD`: Admin user password

### Generate a Strong JWT Secret

**Hex (64 bytes):**

```bash
openssl rand -hex 64
```

**Base64 (64 bytes):**

```bash
openssl rand -base64 64
```

**Python (URL-safe Base64, 64 bytes):**

```bash
python - <<'PY'
import secrets, base64
print(base64.urlsafe_b64encode(secrets.token_bytes(64)).decode())
PY
```

> **Important**: Keep the JWT secret out of git. Set it via environment variables or a local configuration file (ignored by git). Rotating the secret invalidates all existing tokens.

## Getting Started

### Local Development

1. **Start PostgreSQL with Docker Compose:**

   ```bash
   docker compose up -d postgres
   ```

2. **Run the application:**

   ```bash
   ./gradlew bootRun
   ```

   The application will be available at `http://localhost:8085`

   > **Note**: Flyway migrations run automatically on startup. The database schema will be created automatically if it doesn't exist.

### Development with Hot Reload

Use the development Docker Compose setup for hot reload:

```bash
docker compose -f docker-compose.dev.yml up --build
```

This setup:

- Mounts your source code for live reloading
- Includes PostgreSQL
- Uses a strong default JWT secret
- Runs `./gradlew bootRun` for hot reload

### Production-like Container Deployment

1. **Build the container image:**

   ```bash
   ./gradlew jibDockerBuild
   ```

2. **Start the application and database:**

   ```bash
   docker compose up -d
   ```

   The compose file expects the image `baicham/jib-pilot:latest` and exposes the app on port `8080`.

> **Note**: Use one compose file per scenario: `docker-compose.dev.yml` for local development; `docker-compose.yml` with `jibDockerBuild` image for production-like runs.

## API Documentation

<details>
<summary>Click to open</summary>

### Swagger UI

<details>
<summary>Click to open</summary>

Interactive API documentation is available at:

- **Swagger UI**: `http://localhost:8085/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8085/v3/api-docs`

To use Swagger UI:

1. Start the application
2. Navigate to the Swagger UI URL
3. Click "Authorize" and enter your JWT token: `Bearer <your-token>`
4. Explore and test the API endpoints

</details>

### API Endpoints

<details>
<summary>API Endpoints</summary>

#### Authentication Endpoints

| Method | Endpoint                    | Description                  | Auth Required |
|--------|-----------------------------|------------------------------|---------------|
| POST   | `/api/v1/auth/register`      | Register a new student account | No            |
| POST   | `/api/v1/auth/login`         | Login and receive JWT token | No            |

#### Student Endpoints

| Method | Endpoint                                          | Description        | Role Required    |
|--------|---------------------------------------------------|---------------------|------------------|
| POST   | `/api/v1/students`                                | Create a new student | ADMIN            |
| GET    | `/api/v1/students`                                | Get all students   | ADMIN            |
| GET    | `/api/v1/students/{id}`                           | Get student by ID  | ADMIN or OWNER   |
| PUT    | `/api/v1/students/{id}`                           | Update student     | ADMIN or OWNER   |
| DELETE | `/api/v1/students/{id}`                           | Delete student     | ADMIN or OWNER   |
| POST   | `/api/v1/students/{studentId}/courses/{courseId}` | Enroll in course   | ADMIN or OWNER   |
| DELETE | `/api/v1/students/{studentId}/courses/{courseId}` | Drop course        | ADMIN or OWNER   |

#### Course Endpoints

| Method | Endpoint                    | Description                  | Role Required      |
|--------|-----------------------------|------------------------------|--------------------|
| POST   | `/api/v1/courses`            | Create a new course          | ADMIN              |
| GET    | `/api/v1/courses`            | Get all courses              | ADMIN, STUDENT     |
| GET    | `/api/v1/courses/{id}`       | Get course by ID             | ADMIN, STUDENT     |
| PUT    | `/api/v1/courses/{id}`       | Update course                | ADMIN              |
| DELETE | `/api/v1/courses/{id}`       | Delete course                | ADMIN              |
| GET    | `/api/v1/courses/{id}/students` | Get students enrolled in course | ADMIN          |

</details>

</details>

## Authentication & Authorization

<details>
<summary>Click to open</summary>

### Authentication Flow

1. **Register a Student:**

   ```bash
   POST /api/v1/auth/register
   Content-Type: application/json
   
   {
     "firstName": "John",
     "lastName": "Doe",
     "email": "john@example.com",
     "password": "SecurePassword123!"
   }
   ```

2. **Login:**

   ```bash
   POST /api/v1/auth/login
   Content-Type: application/json
   
   {
     "email": "john@example.com",
     "password": "SecurePassword123!"
   }
   ```

   Response:

   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "type": "Bearer",
     "role": "STUDENT",
     "studentId": 1
   }
   ```

3. **Use JWT Token:**
   Add the token to the `Authorization` header:

   ```txt
   Authorization: Bearer <your-token>
   ```

### Roles & Permissions

#### ADMIN Role

- Full access to all student and course operations
- Can create, read, update, and delete any student or course
- Can view all students enrolled in any course
- Can list all students

#### STUDENT Role

- Can only access their own student record
- Can view and update their own profile
- Can enroll in and drop courses (for themselves only)
- Can view all courses (read-only)
- Cannot access other students' data

### Security Features

- **JWT-based Authentication**: Stateless authentication with configurable token expiration
- **Password Encryption**: BCrypt password hashing
- **Method-level Security**: `@PreAuthorize` annotations for fine-grained access control
- **Ownership Validation**: `SecurityGuard` component ensures students can only access their own data
- **CSRF Protection**: Disabled for stateless API (appropriate for JWT-based auth)
- **Session Management**: Stateless sessions (no server-side session storage)
- **Rate Limiting**: Comprehensive rate limiting using Bucket4j to prevent abuse and ensure fair usage

</details>

## Rate Limiting

<details>
<summary>Click to open</summary>

The application implements production-ready rate limiting using **Bucket4j** to protect the API from abuse and ensure fair resource distribution.

### Overview

Rate limiting uses a **token bucket algorithm** that tracks requests per endpoint category and automatically resets limits after the time window expires.

### Rate Limit Categories

The application implements **tiered rate limiting** with different limits for different endpoint types:

#### 1. Authentication Endpoints (Most Strict)

- **Endpoints**: `/api/v1/auth/register`, `/api/v1/auth/login`
- **Limit**: 5 requests per minute per IP address
- **Purpose**: Prevents brute force attacks and account enumeration

#### 2. Authenticated Endpoints (Higher)

- **Endpoints**: All `/api/v1/students/**` and `/api/v1/courses/**` endpoints
- **Limit**: 1000 requests per minute per IP address
- **Purpose**: Allows legitimate high-volume usage while preventing abuse

### Excluded Endpoints

The following endpoints are **not rate limited**:

- `/actuator/**` - Monitoring endpoints
- `/swagger-ui/**`, `/swagger-ui.html` - API documentation
- `/v3/api-docs/**` - OpenAPI specification
- `/swagger-resources/**` - Swagger resources
- `/webjars/**` - WebJars resources

### Configuration

Rate limiting is configured in `application.yml`:

```yaml
app:
  rate-limiting:
    enabled: true
    auth:
      requests-per-minute: 5
    public:
      requests-per-minute: 100
    authenticated:
      requests-per-minute: 1000
```

**Configuration Options:**

- `enabled`: Enable or disable rate limiting (default: `true`)
- `auth.requests-per-minute`: Limit for authentication endpoints (default: `5`)
- `public.requests-per-minute`: Limit for public endpoints (default: `100`, currently unused)
- `authenticated.requests-per-minute`: Limit for authenticated endpoints (default: `1000`)

### Rate Limit Headers

When a request is made, the response includes rate limit headers:

- `X-RateLimit-Limit`: Maximum number of requests allowed per time window
- `X-RateLimit-Remaining`: Number of requests remaining in the current window
- `X-RateLimit-Reset`: Unix timestamp when the rate limit resets

**Example Response Headers:**

```txt
X-RateLimit-Limit: 5
X-RateLimit-Remaining: 3
X-RateLimit-Reset: 1704067200
```

### Rate Limit Exceeded Response

When a rate limit is exceeded, the API returns:

**Status Code**: `429 Too Many Requests`

**Response Body:**

```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later.",
  "retryAfter": 60
}
```

**Headers:**

- `Retry-After`: Number of seconds to wait before retrying (typically 60)
- `X-RateLimit-Limit`: The limit that was exceeded
- `X-RateLimit-Remaining`: 0
- `X-RateLimit-Reset`: When the limit will reset

### How Rate Limits Are Applied

1. **IP-based for All Requests**:
   - Rate limiting uses the client's IP address
   - Handles proxies and load balancers via `X-Forwarded-For` and `X-Real-IP` headers

2. **Automatic Cleanup**:
   - Rate limit buckets are stored in-memory
   - Unused buckets are automatically cleaned up

### Testing Rate Limiting

**Test rate limit enforcement:**

```bash
# Make multiple requests quickly
for i in {1..10}; do
  curl -X POST http://localhost:8085/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"test"}'
done
```

**Check rate limit headers:**

```bash
curl -v http://localhost:8085/api/v1/auth/login
# Look for X-RateLimit-* headers in response
```

### Disabling Rate Limiting

To disable rate limiting (not recommended for production):

```yaml
app:
  rate-limiting:
    enabled: false
```

### Troubleshooting

**Issue**: Receiving 429 errors frequently

- **Solution**: Check your request rate and consider increasing limits in configuration
- **Solution**: Implement request batching or caching to reduce API calls

**Issue**: Rate limits not working

- **Solution**: Verify `app.rate-limiting.enabled` is set to `true`
- **Solution**: Check application logs for rate limiting errors

</details>

## Actuator Endpoints

<details>
<summary>Click to open</summary>

Spring Boot Actuator provides production-ready features for monitoring and managing the application. All actuator endpoints are publicly accessible at `/actuator/*`.

### Standard Actuator Endpoints

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment properties
- `/actuator/loggers` - Logger configuration
- `/actuator/flyway` - Flyway migration status

### Custom Endpoints

#### User Numbers Endpoint

**Endpoint**: `GET /actuator/userNumbers`

Returns the total number of users in the database.

**Response:**

```json
{
  "user-count": 5
}
```

**Example:**

```bash
curl http://localhost:8085/actuator/userNumbers
```

</details>

## Testing

<details>
<summary>Click to open</summary>

### Run All Tests

```bash
./gradlew test
```

This runs all tests, including Spring Boot integration tests.

### Controller Slice Tests

These tasks run `@WebMvcTest` slices only (faster, lighter context):

```bash
./gradlew courseControllerTest
./gradlew studentControllerTest
```

- `courseControllerTest`: Tests tagged with `@Tag("courseControllerTest")` (e.g., `CourseControllerTest`)
- `studentControllerTest`: Tests tagged with `@Tag("studentControllerTest")` (e.g., `StudentControllerTest`)

**What gets tested:**

- Controller endpoints and request/response handling
- HTTP status codes and headers
- JSON serialization/deserialization
- Security annotations and method-level authorization

### Spring Boot Integration Tests

Integration tests are tagged `@Tag("springBoot")` (e.g., `CourseControllerIntTest`, `StudentControllerIntTest`) and run as part of `./gradlew test`.

**What gets tested:**

- Full application context integration
- All Spring components (security, filters, configuration)
- End-to-end request handling through the complete filter chain
- Integration with all Spring Boot auto-configurations

**Run a specific test class:**

```bash
./gradlew test --tests 'com.abc.jibpilot.course.controller.CourseControllerIntTest'
```

### Test Reports

All test tasks generate HTML and XML test reports:

**HTML Reports:**

- All Tests: `build/reports/tests/test/index.html`
- Controller Tests: `build/reports/tests/courseControllerTest/index.html` and `build/reports/tests/studentControllerTest/index.html`

**JUnit XML Reports:**

- `build/test-results/test/`
- `build/test-results/courseControllerTest/`
- `build/test-results/studentControllerTest/`

**View Reports:**
Open the `index.html` file in any web browser to view detailed test results. Reports are automatically generated after each test run.

### Test Configuration

Test-specific configuration is in `src/test/resources/application.properties`:

- Uses H2 in-memory database
- JWT secret configured for testing
- Docker Compose disabled for tests

### Test Coverage

The project includes comprehensive test coverage:

- **Controller Tests**: MockMvc-based integration tests for all REST endpoints
- **Service Tests**: Unit tests for business logic
- **Test Database**: H2 in-memory database for fast test execution

</details>

## SonarQube Code Quality Analysis

<details>
<summary>Click to open</summary>

SonarQube is integrated into this project to provide continuous code quality analysis, detecting bugs, code smells, security vulnerabilities, and technical debt. The project uses SonarQube Community Edition, which runs via Docker Compose.

### Overview

SonarQube analyzes your codebase and provides:

- **Code Quality Metrics**: Maintainability, reliability, and security ratings
- **Code Smell Detection**: Identifies code that works but could be improved
- **Bug Detection**: Finds potential runtime errors
- **Security Vulnerabilities**: Detects security hotspots and vulnerabilities
- **Code Coverage**: Tracks test coverage across the codebase
- **Technical Debt**: Estimates effort needed to fix issues

### Prerequisites

- **Docker** and **Docker Compose** (already required for the project)
- **SonarQube Server** running (via Docker Compose)

### Starting SonarQube Server

Start SonarQube using Docker Compose:

```bash
docker compose up -d sonarqube
```

The SonarQube web UI will be available at `http://localhost:9000` after the service starts (typically takes 30-60 seconds).

**Default Credentials:**

- Username: `admin`
- Password: `admin` (you'll be prompted to change this on first login)

**Check SonarQube Status:**

```bash
docker compose ps sonarqube
```

**View SonarQube Logs:**

```bash
docker compose logs -f sonarqube
```

### Generating Authentication Token

Before running analysis, you need to generate an authentication token:

1. **Access SonarQube Web UI**: Navigate to `http://localhost:9000`
2. **Login**: Use default credentials (`admin`/`admin`) or your configured credentials
3. **Create Token**:
   - Click on your profile icon (top right) → **My Account**
   - Go to the **Security** tab
   - Under **Generate Tokens**, enter a token name (e.g., "jib-pilot-analysis")
   - Click **Generate**
   - **Copy the token immediately** (it won't be shown again)

4. **Set Environment Variable:**

   ```bash
   export SONAR_TOKEN=your_generated_token_here
   ```

   Or on Windows (PowerShell):

   ```powershell
   $env:SONAR_TOKEN="your_generated_token_here"
   ```

   Or on Windows (CMD):

   ```cmd
   set SONAR_TOKEN=your_generated_token_here
   ```

### Running Analysis

Once SonarQube is running and you have a token configured, run code analysis:

**Run analysis only:**

```bash
./gradlew sonarqube
```

**Build and analyze:**

```bash
./gradlew build sonarqube
```

**With custom SonarQube URL:**

```bash
./gradlew sonarqube -Dsonar.host.url=http://your-sonarqube-server:9000
```

The analysis will:

1. Compile and analyze your Java source code
2. Run tests (if not already run)
3. Calculate code coverage
4. Upload results to SonarQube server
5. Wait for quality gate evaluation (if configured)

### Viewing Results

After analysis completes:

1. **Access SonarQube Web UI**: `http://localhost:9000`
2. **Navigate to Project**: Find "Jib Pilot" in the projects list
3. **View Analysis Results**:
   - **Overview**: Project summary with quality gate status
   - **Issues**: List of bugs, vulnerabilities, and code smells
   - **Measures**: Code metrics, coverage, and technical debt
   - **Code**: Source code with inline issue markers

**Quality Gate Status:**

- ✅ **Passed**: Code meets quality standards
- ❌ **Failed**: Quality gate conditions not met (e.g., too many issues, low coverage)

### Configuration

SonarQube is configured in `build.gradle`:

```groovy
sonarqube {
    properties {
        property "sonar.projectKey", "com.abc:jib-pilot"
        property "sonar.projectName", "Jib Pilot"
        property "sonar.projectVersion", project.version
        property "sonar.host.url", System.getProperty("sonar.host.url", "http://localhost:9000")
        property "sonar.login", System.getenv("SONAR_TOKEN") ?: System.getProperty("sonar.login", "")
        property "sonar.sourceEncoding", "UTF-8"
        property "sonar.java.source", "21"
        property "sonar.java.target", "21"
        property "sonar.qualitygate.wait", "true"
        property "sonar.qualitygate.timeout", "600"
    }
}
```

**Key Properties:**

- `sonar.projectKey`: Unique identifier for the project in SonarQube
- `sonar.host.url`: SonarQube server URL (defaults to `http://localhost:9000`)
- `sonar.login`: Authentication token (from `SONAR_TOKEN` environment variable)
- `sonar.qualitygate.wait`: Wait for quality gate evaluation before completing
- `sonar.qualitygate.timeout`: Maximum wait time in seconds (default: 600)

**Customizing Configuration:**

- Override `sonar.host.url` via system property: `-Dsonar.host.url=http://custom-url:9000`
- Override token via system property: `-Dsonar.login=your-token`
- Modify properties in `build.gradle` for project-specific settings

### Troubleshooting

**Issue**: SonarQube service won't start

- **Solution**: Check Docker resources (SonarQube requires at least 2GB RAM)
- **Solution**: Verify port 9000 is not already in use
- **Solution**: Check logs: `docker compose logs sonarqube`

**Issue**: Analysis fails with authentication error

- **Solution**: Verify `SONAR_TOKEN` environment variable is set correctly
- **Solution**: Regenerate token in SonarQube UI if expired
- **Solution**: Check token has proper permissions in SonarQube

**Issue**: Quality gate timeout

- **Solution**: Increase `sonar.qualitygate.timeout` in `build.gradle`
- **Solution**: Disable quality gate wait: remove `sonar.qualitygate.wait` property

**Issue**: Cannot connect to SonarQube server

- **Solution**: Verify SonarQube is running: `docker compose ps sonarqube`
- **Solution**: Check SonarQube URL matches server address
- **Solution**: Verify network connectivity to SonarQube server

**Issue**: First analysis takes very long

- **Solution**: This is normal - SonarQube indexes the codebase on first run
- **Solution**: Subsequent analyses are faster

</details>

## Building & Deployment

### Build JAR

```bash
./gradlew build
```

The JAR file will be created in `build/libs/jib-pilot-0.0.1.jar`

### Container Image with Jib

**Build without pushing:**

```bash
./gradlew jib
```

**Build and push to Docker Hub:**

```bash
./gradlew jib -PdockerHubUsername=baicham -PdockerHubPassword=your_password
```

Or use environment variables:

```bash
export DOCKER_HUB_USERNAME=baicham
export DOCKER_HUB_PASSWORD=your_password
./gradlew jib
```

**Build to local Docker daemon:**

```bash
./gradlew jibDockerBuild
```

### Jib Configuration

The Jib configuration in `build.gradle` includes:

- Base image: `eclipse-temurin:21-jre-alpine`
- Container port: `8080`
- JVM flags optimized for containers
- OCI image format
- Automatic layer caching for Spring Boot

## Database Schema

### Entities

#### UserAccount

- `id` (Long, Primary Key)
- `email` (String, Unique, Not Null)
- `password` (String, BCrypt hashed, Not Null)
- `role` (Role enum: ADMIN, STUDENT, Not Null)
- `student` (Student, One-to-One, Optional)

#### Student

- `id` (Long, Primary Key)
- `firstName` (String, Not Null)
- `lastName` (String, Not Null)
- `email` (String, Unique, Not Null)
- `courses` (Set<Course>, Many-to-Many)
- `userAccount` (UserAccount, One-to-One, Optional)

#### Course

- `id` (Long, Primary Key)
- `code` (String, Unique, Not Null)
- `title` (String, Not Null)
- `description` (String)
- `students` (Set<Student>, Many-to-Many)

### Relationships

- **UserAccount ↔ Student**: One-to-One (optional)
- **Student ↔ Course**: Many-to-Many (enrollment relationship)

## Flyway Database Migrations

<details>
<summary>Click to open</summary>

This application uses **Flyway** for database schema version control and migration management. Migrations run **automatically on application startup** - no manual steps required.

### Overview

Flyway manages database migrations automatically, ensuring:

- **Version control** for database schema changes
- **Reproducible deployments** across environments
- **Safe migrations** with validation and rollback capabilities
- **Team collaboration** with shared migration history

### Automatic Execution

**Migrations run automatically when the application starts.** You don't need to run migrations manually before starting the app.

**What happens on startup:**

1. Flyway checks the database for the `flyway_schema_history` table
2. If the table doesn't exist, it creates it (or baselines if `baseline-on-migrate: true`)
3. It scans `classpath:db/migration` for migration files
4. It compares found migrations with what's already applied
5. It runs any pending migrations in order
6. It validates checksums of applied migrations
7. The application continues starting only after migrations complete successfully

**Important Notes:**

- If a migration fails, the app won't start (fail-fast behavior)
- Migrations run before the application context fully initializes
- Each migration runs in its own transaction (rolls back on failure)
- The `baseline-on-migrate: true` setting handles existing databases that don't have Flyway tracking yet

### Configuration

#### Dependencies

Flyway is included in `build.gradle`:

```gradle
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-database-postgresql'
```

#### Application Configuration

Flyway is configured in `application.yml`:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    validate-on-migrate: true
    clean-disabled: true
    encoding: UTF-8
    table: flyway_schema_history
    mixed: true
    group: false
```

#### Profile-Specific Settings

**Development (`application-dev.yml`):**

- `clean-disabled: false` - Allows clean operations (use with caution)
- `out-of-order: false` - Migrations must run in order
- More verbose logging (`org.flywaydb: DEBUG`)

**Production (`application-prod.yml`):**

- `clean-disabled: true` - Clean operations disabled for safety
- `validate-on-migrate: true` - Strict validation
- `baseline-on-migrate: true` - Baseline existing databases
- `out-of-order: false` - Migrations must run in order

### Migration Files

Migration files are located in: `src/main/resources/db/migration/`

#### Naming Convention

Flyway migrations follow this naming pattern:

- **Versioned migrations**: `V{version}__{description}.sql`
  - Example: `V1__Initial_schema.sql`
  - Example: `V2__Add_user_indexes.sql`
- **Repeatable migrations**: `R__{description}.sql`
  - Example: `R__Update_views.sql`

#### Current Migrations

1. **V1__Initial_schema.sql**: Creates the initial database schema
   - `students` table
   - `users` table
   - `courses` table
   - `student_courses` join table
   - Indexes for performance
   - Triggers for `updated_at` timestamps
   - Constraints and foreign keys

### Hibernate Alignment

Hibernate is configured to work with Flyway:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Prevents Hibernate from modifying schema
```

**Important**: Hibernate's `ddl-auto` is set to `validate` to ensure Flyway is the sole schema manager. This prevents accidental schema changes.

### Creating New Migrations

#### Step 1: Create Migration File

Create a new SQL file in `src/main/resources/db/migration/` following the naming convention:

```sql
-- V2__Add_user_indexes.sql
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
```

#### Step 2: Test Locally

1. Start your local database:

   ```bash
   docker compose up -d postgres
   ```

2. Run the application:

   ```bash
   ./gradlew bootRun
   ```

   The migration will run automatically on startup.

3. Verify the migration was applied:

   ```sql
   SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;
   ```

#### Step 3: Commit and Deploy

1. Commit the migration file to version control
2. Deploy to your environment
3. Flyway will automatically apply the migration on startup

### Manual Migration (Optional)

You can also run migrations manually using Flyway CLI (if installed):

```bash
flyway migrate -url=jdbc:postgresql://localhost:5432/jib_pilot -user=postgres -password=postgres
```

> **Note**: Manual migration is typically not needed since migrations run automatically on startup.

### Best Practices

1. **Always Test Migrations Locally**: Test migrations on a copy of production data before deploying
2. **Use Transactions**: Flyway wraps each migration in a transaction - if a migration fails, it's automatically rolled back
3. **Never Modify Applied Migrations**: Once a migration is applied, never change it - create a new migration to fix issues
4. **Use Idempotent Operations**: Prefer `CREATE TABLE IF NOT EXISTS` and `CREATE INDEX IF NOT EXISTS` when appropriate
5. **Baseline Existing Databases**: If you have an existing database, use `baseline-on-migrate: true` or manually baseline: `flyway baseline`
6. **Monitor Migration Status**: Check `/actuator/flyway` endpoint or review `flyway_schema_history` table

### Monitoring

- **Actuator Endpoint**: `/actuator/flyway` is exposed when `management.endpoints.web.exposure.include` contains `flyway` (this project uses `*`)
- **Schema History Table**: Review `flyway_schema_history` table for migration status
- **Application Logs**: Monitor application logs for migration status and errors

### Troubleshooting

**Migration fails on startup:**

- Check Flyway logs and connection credentials
- Validate SQL syntax
- Inspect `flyway_schema_history` table
- Verify database connection is working

**Checksum mismatch:**

- A migration file was modified after being applied
- **Solution**: Create a new migration instead of modifying the old one
- Or repair: `flyway repair` (use with caution)

**Schema out of sync:**

- Compare migration files with `flyway_schema_history` table
- Add a corrective migration or baseline existing databases
- Consider using `flyway baseline` for existing databases

**Database connection issues:**

- Verify database credentials in `application.yml`
- Ensure PostgreSQL is running: `docker compose ps postgres`
- Check database exists: `docker compose exec postgres psql -U postgres -l`

### Migration History

The `flyway_schema_history` table tracks all applied migrations:

```sql
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
```

This table is automatically maintained by Flyway and should not be manually modified.

### Additional Resources

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Flyway Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)

</details>

## Security Considerations

1. **JWT Secret**: Always use a strong, randomly generated secret. Never commit secrets to version control.

2. **Password Policy**: Consider implementing password complexity requirements in production.

3. **HTTPS**: Use HTTPS in production to protect JWT tokens in transit.

4. **Token Expiration**: Default token expiration is 3600 seconds (1 hour). Adjust based on your security requirements.

5. **Admin Credentials**: Change default admin credentials before deploying to production.

6. **Database Credentials**: Use strong database passwords and restrict database access.

7. **CORS**: Configure CORS appropriately for your frontend application.

8. **Rate Limiting**: Rate limiting is enabled by default to prevent abuse.

## Development Notes

- **Email Uniqueness**: Email uniqueness is enforced across both `Student` and `UserAccount` entities
- **DTO Pattern**: Requests use `Create/Update...Request` naming, responses use `...Response`
- **Location Headers**: Create operations return `201 Created` with a `Location` header pointing to the new resource
- **No Body on Create**: Create endpoints return only the Location header (no body) to prevent reflection attacks
- **Docker Compose**: Spring Boot Docker Compose support is disabled in containerized environments to avoid conflicts
- **Flyway Migrations**: Migrations run automatically on startup - no manual steps required

## Troubleshooting

### Database Connection Issues

- Ensure PostgreSQL is running: `docker compose ps`
- Check database credentials in `application.yml`
- Verify database exists: `docker compose exec postgres psql -U postgres -l`

### JWT Token Issues

- Verify JWT secret is set correctly
- Check token expiration time
- Ensure token is included in `Authorization: Bearer <token>` header

### Port Conflicts

- Default application port is `8085`
- Change in `application.yml` if needed
- Docker Compose uses port `8080` for the containerized app

### Migration Issues

- Check Flyway logs in application startup logs
- Verify migration files are in `src/main/resources/db/migration/`
- Review `flyway_schema_history` table for applied migrations
- See Flyway section above for detailed troubleshooting

## License

[Add your license information here]

## Contributing

[Add contribution guidelines here]

## Support

[Add support information here]
