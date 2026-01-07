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
- **Database Migrations**: Automatic schema management with Hibernate
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
│   │   │   │   ├── filter/        # JWT authentication filter
│   │   │   │   ├── model/         # Security models (Role, AppUserDetails)
│   │   │   │   ├── repository/    # User repository
│   │   │   │   └── service/       # Auth services
│   │   │   ├── config/            # Configuration classes
│   │   │   ├── course/            # Course management
│   │   │   ├── security/          # Security utilities
│   │   │   └── student/           # Student management
│   │   └── resources/ 
│   │       ├── application.yml    # Application configuration
│   │       ├── newrelic.yml       # New Relic configuration
│   │       └── static/            # Static resources
│   └── test/                      # Test classes
├── build.gradle                   # Build configuration
├── compose.yml                    # Development Docker Compose file
└── Dockerfile                     # Development Dockerfile
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
   docker compose up -d
   ```

2. **Run the application:**

   ```bash
   ./gradlew bootRun
   ```

   The application will be available at `http://localhost:8085`

### Development with Hot Reload

Use the development Docker Compose setup for hot reload:

```bash
docker compose -f compose.yml up --build
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

> **Note**: Use one compose file per scenario: `compose.yml` for local development; `docker-compose.yml` with `jibDockerBuild` image for production-like runs.

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

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/register` | Register a new student account | No |
| POST | `/api/v1/auth/login` | Login and receive JWT token | No |

#### Student Endpoints

| Method | Endpoint | Description | Role Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/students` | Create a new student | ADMIN |
| GET | `/api/v1/students` | Get all students | ADMIN |
| GET | `/api/v1/students/{id}` | Get student by ID | ADMIN or OWNER |
| PUT | `/api/v1/students/{id}` | Update student | ADMIN or OWNER |
| DELETE | `/api/v1/students/{id}` | Delete student | ADMIN or OWNER |
| POST | `/api/v1/students/{studentId}/courses/{courseId}` | Enroll in course | ADMIN or OWNER |
| DELETE | `/api/v1/students/{studentId}/courses/{courseId}` | Drop course | ADMIN or OWNER |

#### Course Endpoints

| Method | Endpoint | Description | Role Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/courses` | Create a new course | ADMIN |
| GET | `/api/v1/courses` | Get all courses | ADMIN, STUDENT |
| GET | `/api/v1/courses/{id}` | Get course by ID | ADMIN, STUDENT |
| PUT | `/api/v1/courses/{id}` | Update course | ADMIN |
| DELETE | `/api/v1/courses/{id}` | Delete course | ADMIN |
| GET | `/api/v1/courses/{id}/students` | Get students enrolled in course | ADMIN |

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

The application implements production-ready rate limiting using **Bucket4j**, an industry-standard library for rate limiting in Java applications. Rate limiting protects the API from abuse, prevents brute force attacks, and ensures fair resource distribution.

### How It Works

Rate limiting uses a **token bucket algorithm** that:

- Tracks requests per endpoint category
- Uses IP addresses for unauthenticated requests
- Uses user IDs for authenticated requests
- Automatically resets limits after the time window expires
- Returns HTTP 429 (Too Many Requests) when limits are exceeded

### Rate Limit Categories

The application implements **tiered rate limiting** with three categories:

#### 1. Authentication Endpoints (Most Strict)

- **Endpoints**: `/api/v1/auth/register`, `/api/v1/auth/login`
- **Limit**: 5 requests per minute per IP address
- **Purpose**: Prevents brute force attacks and account enumeration

#### 2. Public Endpoints (Moderate)

- **Endpoints**: `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- **Limit**: 100 requests per minute per IP address
- **Purpose**: Prevents abuse while allowing legitimate monitoring and documentation access

#### 3. Authenticated Endpoints (Higher)

- **Endpoints**: All `/api/v1/students/**` and `/api/v1/courses/**` endpoints
- **Limit**: 1000 requests per minute per authenticated user
- **Purpose**: Allows legitimate high-volume usage while preventing abuse

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
- `public.requests-per-minute`: Limit for public endpoints (default: `100`)
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

1. **IP-based for Unauthenticated Requests**:
   - Authentication and public endpoints use the client's IP address
   - Handles proxies and load balancers via `X-Forwarded-For` and `X-Real-IP` headers

2. **User-based for Authenticated Requests**:
   - Authenticated endpoints use the user's ID from the JWT token
   - Each authenticated user has their own rate limit bucket

3. **Automatic Cleanup**:
   - Rate limit buckets are stored in-memory
   - Unused buckets are automatically cleaned up

### Best Practices

1. **Monitor Rate Limit Headers**: Check `X-RateLimit-Remaining` to avoid hitting limits
2. **Implement Exponential Backoff**: When receiving 429 responses, wait before retrying
3. **Cache Responses**: Reduce API calls by caching responses when appropriate
4. **Batch Requests**: Combine multiple operations into single requests when possible

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

**Issue**: Different limits needed for different environments

- **Solution**: Use Spring profiles to configure different limits per environment

</details>

## Rate Limiting Implementation Guide

<details>
<summary>Click to open</summary>

This section provides a detailed technical guide on how rate limiting is implemented in this application, including architecture, key components, and customization options.

### Architecture Overview

The rate limiting implementation uses a **filter-based approach** integrated with Spring Security's filter chain:

```txt
Request → RateLimitingFilter → JwtAuthenticationFilter → Controller
           ↓
    [Check Rate Limit]
           ↓
    [Allow/Block Request]
```

### Key Components

#### 1. RateLimitingConfig (`src/main/java/com/abc/jibpilot/ratelimit/RateLimitingConfig.java`)

Configuration class that reads rate limiting settings from `application.yml`:

```java
@Configuration
public class RateLimitingConfig {
    @Value("${app.rate-limiting.enabled:true}")
    private boolean rateLimitingEnabled;
    
    @Value("${app.rate-limiting.auth.requests-per-minute:5}")
    private int authRequestsPerMinute;
    // ... other limits
}
```

**Responsibilities:**

- Loads configuration from application properties
- Provides getter methods for rate limit values
- Centralizes rate limiting configuration

#### 2. RateLimitKeyResolver (`src/main/java/com/abc/jibpilot/ratelimit/RateLimitKeyResolver.java`)

Resolves the key used to identify rate limit buckets:

```java
@Component
public class RateLimitKeyResolver {
    public String resolveKey(HttpServletRequest request) {
        // For authenticated users: returns "user:{userId}"
        // For unauthenticated: returns "ip:{ipAddress}"
    }
}
```

**Key Resolution Strategy:**

- **Authenticated requests**: Uses user ID from Spring Security context (`user:{userId}`)
- **Unauthenticated requests**: Uses client IP address (`ip:{ipAddress}`)
- **IP Extraction**: Handles proxies/load balancers via `X-Forwarded-For` and `X-Real-IP` headers

#### 3. RateLimitingFilter (`src/main/java/com/abc/jibpilot/ratelimit/RateLimitingFilter.java`)

Main filter that implements rate limiting logic:

**Key Features:**

- Extends `OncePerRequestFilter` for Spring integration
- Uses Bucket4j's token bucket algorithm
- Maintains separate bucket maps for each endpoint category
- Adds rate limit headers to all responses
- Returns 429 status with JSON error when limit exceeded

**Bucket Storage:**

```java
private final ConcurrentMap<String, LocalBucket> authBuckets = new ConcurrentHashMap<>();
private final ConcurrentMap<String, LocalBucket> publicBuckets = new ConcurrentHashMap<>();
private final ConcurrentMap<String, LocalBucket> authenticatedBuckets = new ConcurrentHashMap<>();
```

**Filter Flow:**

1. Check if rate limiting is enabled
2. Determine endpoint category (AUTH, PUBLIC, AUTHENTICATED, NONE)
3. Resolve rate limit key (IP or user ID)
4. Get or create bucket for the key
5. Try to consume a token
6. If successful: add headers and continue
7. If failed: return 429 response

#### 4. Bucket4j Integration

**Bucket Creation:**

```java
Bandwidth limit = Bandwidth.builder()
    .capacity(capacity)
    .refillIntervally(capacity, Duration.ofMinutes(1))
    .build();

LocalBucket bucket = Bucket.builder()
    .addLimit(limit)
    .build();
```

**Token Consumption:**

```java
if (bucket.tryConsume(1)) {
    // Request allowed
} else {
    // Rate limit exceeded
}
```

### Code Walkthrough

#### Endpoint Category Detection

```java
private EndpointCategory determineEndpointCategory(String requestUri) {
    if (requestUri.startsWith("/api/v1/auth/")) {
        return EndpointCategory.AUTH;
    } else if (requestUri.startsWith("/actuator/") || 
               requestUri.startsWith("/swagger-ui") || 
               requestUri.startsWith("/v3/api-docs")) {
        return EndpointCategory.PUBLIC;
    } else if (requestUri.startsWith("/api/v1/")) {
        return EndpointCategory.AUTHENTICATED;
    }
    return EndpointCategory.NONE;
}
```

#### Rate Limit Check

```java
// Get or create bucket for this key
ConcurrentMap<String, LocalBucket> bucketMap = getBucketMap(category);
LocalBucket bucket = bucketMap.computeIfAbsent(key, k -> createBucketForCategory(category));

// Try to consume a token
if (bucket.tryConsume(1)) {
    addRateLimitHeaders(response, bucket, category);
    filterChain.doFilter(request, response);
} else {
    handleRateLimitExceeded(response, bucket, category);
}
```

#### Response Headers

```java
private void addRateLimitHeaders(HttpServletResponse response, LocalBucket bucket, EndpointCategory category) {
    long availableTokens = bucket.getAvailableTokens();
    response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
    response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));
    response.setHeader("X-RateLimit-Reset", String.valueOf(Instant.now().plusSeconds(60).getEpochSecond()));
}
```

### Integration with Spring Security

The rate limiting filter is integrated into Spring Security's filter chain in `SecurityConfig`:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // ... other configuration
        .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

**Filter Order:**

1. Rate Limiting Filter (runs first, before authentication)
2. JWT Authentication Filter
3. Standard Authentication Filter

This order ensures rate limiting happens before authentication processing, which is more efficient.

### Customization Options

#### 1. Adding New Endpoint Categories

To add a new rate limit category:

1. **Add configuration property** in `application.yml`:

```yaml
app:
  rate-limiting:
    new-category:
      requests-per-minute: 50
```

1. **Update RateLimitingConfig**:

```java
@Value("${app.rate-limiting.new-category.requests-per-minute:50}")
private int newCategoryRequestsPerMinute;
```

1. **Add enum value** in `RateLimitingFilter`:

```java
private enum EndpointCategory {
    AUTH, PUBLIC, AUTHENTICATED, NEW_CATEGORY, NONE
}
```

1. **Update category detection**:

```java
if (requestUri.startsWith("/api/v1/new-endpoint/")) {
    return EndpointCategory.NEW_CATEGORY;
}
```

#### 2. Changing Rate Limit Algorithm

Currently using **intervally refill** (tokens refill all at once). To use **smooth refill** (gradual refill):

```java
Bandwidth limit = Bandwidth.builder()
    .capacity(capacity)
    .refillGreedy(capacity, Duration.ofMinutes(1))  // All at once
    // OR
    .refillSmooth(capacity, Duration.ofMinutes(1))  // Gradual
    .build();
```

#### 3. Custom Key Resolution

To implement custom key resolution logic, modify `RateLimitKeyResolver`:

```java
public String resolveKey(HttpServletRequest request) {
    // Custom logic: e.g., combine IP + User-Agent
    String ip = getClientIpAddress(request);
    String userAgent = request.getHeader("User-Agent");
    return "ip:" + ip + ":ua:" + hash(userAgent);
}
```

#### 4. Distributed Rate Limiting (Redis)

To switch from in-memory to distributed rate limiting with Redis:

1. **Add Redis dependency**:

```gradle
implementation 'com.bucket4j:bucket4j-redis:8.10.1'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

1. **Update bucket storage** to use Redis:

```java
// Replace ConcurrentMap with Redis-backed storage
private final RedisTemplate<String, Bucket> redisTemplate;
```

1. **Configure Redis connection** in `application.yml`

### Extension Points

#### 1. Custom Error Response Format

Modify `RateLimitErrorResponse` DTO to include additional fields:

```java
public record RateLimitErrorResponse(
    String error,
    String message,
    Long retryAfter,
    String endpoint,        // New field
    Long limit,            // New field
    Long remaining          // New field
) { }
```

#### 2. Rate Limit Metrics

Add metrics collection for monitoring:

```java
private void recordRateLimitHit(EndpointCategory category, boolean exceeded) {
    meterRegistry.counter("rate.limit.hits", 
        "category", category.name(),
        "exceeded", String.valueOf(exceeded)
    ).increment();
}
```

#### 3. Whitelist/Blacklist

Add IP whitelist/blacklist functionality:

```java
private boolean isWhitelisted(String ip) {
    return whitelist.contains(ip);
}

private boolean isBlacklisted(String ip) {
    return blacklist.contains(ip);
}
```

### Testing Rate Limiting

#### Manual Testing

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

#### Unit Testing

Example test for rate limiting filter:

```java
@Test
void testRateLimitExceeded() throws Exception {
    // Make requests up to the limit
    for (int i = 0; i < 5; i++) {
        mockMvc.perform(post("/api/v1/auth/login"))
            .andExpect(status().isOk());
    }
    
    // Next request should be rate limited
    mockMvc.perform(post("/api/v1/auth/login"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().string("X-RateLimit-Remaining", "0"));
}
```

### Performance Considerations

1. **In-Memory Storage**: Current implementation uses `ConcurrentHashMap` for O(1) lookups
2. **Bucket Cleanup**: Unused buckets remain in memory; consider periodic cleanup for long-running applications
3. **Thread Safety**: `ConcurrentHashMap` and Bucket4j's `LocalBucket` are thread-safe
4. **Memory Usage**: Each bucket consumes minimal memory (~100 bytes); 10,000 buckets ≈ 1MB

### Future Enhancements

Potential improvements for production use:

1. **Redis Integration**: For distributed rate limiting across multiple instances
2. **Sliding Window**: More accurate rate limiting with sliding time windows
3. **Dynamic Limits**: Adjust limits based on system load or user tier
4. **Rate Limit Metrics**: Expose metrics via Actuator for monitoring
5. **IP Geolocation**: Different limits based on geographic location
6. **Rate Limit Dashboard**: Visual monitoring of rate limit usage

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

### Run All Tests

```bash
./gradlew test
```

### Conditional Test Execution

The project uses JUnit tags to organize tests into different categories. You can run specific test suites conditionally:

#### Run Only WebMvcTest Tests

Tests tagged with `@Tag("webmvc")` use `@WebMvcTest`, which only loads the web layer for faster execution:

```bash
./gradlew webmvcTest
```

**What gets tested:**
- Controller endpoints and request/response handling
- HTTP status codes and headers
- JSON serialization/deserialization
- Security annotations and method-level authorization

**Test files:**
- `StudentControllerTest`
- `CourseControllerTest`

#### Run Only SpringBootTest Integration Tests

Tests tagged with `@Tag("spring-boot")` use `@SpringBootTest`, which loads the full Spring application context:

```bash
./gradlew springBootTest
```

**What gets tested:**
- Full application context integration
- All Spring components (security, filters, configuration)
- End-to-end request handling through the complete filter chain
- Integration with all Spring Boot auto-configurations

**Test files:**
- `StudentControllerIntTest`
- `CourseControllerIntTest`

#### Test Type Comparison

| Feature | @WebMvcTest | @SpringBootTest |
|---------|-------------|-----------------|
| **Context Loading** | Web layer only | Full application context |
| **Speed** | Faster (lighter context) | Slower (full context) |
| **Use Case** | Controller unit testing | Integration testing |
| **Spring Components** | Controllers, filters, security | All beans and configurations |
| **When to Use** | Fast feedback during development | Verify full integration |

Both test types use mocked services (`@MockitoBean`) to keep tests isolated and fast, while `@SpringBootTest` provides a more realistic integration test environment.

### Test Reports

All test tasks generate HTML and XML test reports:

**Report Locations:**
- **All Tests**: `build/reports/tests/test/index.html`
- **WebMvcTest Tests**: `build/reports/tests/webmvcTest/index.html`
- **SpringBootTest Tests**: `build/reports/tests/springBootTest/index.html`

**Report Contents:**
- Test execution summary (passed, failed, skipped)
- Individual test results with execution time
- Test output and stack traces for failures
- Package and class-level test organization

**View Reports:**
Open the `index.html` file in any web browser to view detailed test results. Reports are automatically generated after each test run.

**XML Reports:**
JUnit XML reports are also generated in `build/test-results/` for CI/CD integration:
- `build/test-results/test/`
- `build/test-results/webmvcTest/`
- `build/test-results/springBootTest/`

### Test Coverage

The project includes comprehensive test coverage:

- **Controller Tests**: MockMvc-based integration tests for all REST endpoints
- **Service Tests**: Unit tests for business logic
- **Test Database**: H2 in-memory database for fast test execution

### Test Configuration

Test-specific configuration is in `src/test/resources/application.yml`:

- Uses H2 in-memory database
- JWT secret configured for testing
- Docker Compose disabled for tests

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

## Security Considerations

1. **JWT Secret**: Always use a strong, randomly generated secret. Never commit secrets to version control.

2. **Password Policy**: Consider implementing password complexity requirements in production.

3. **HTTPS**: Use HTTPS in production to protect JWT tokens in transit.

4. **Token Expiration**: Default token expiration is 3600 seconds (1 hour). Adjust based on your security requirements.

5. **Admin Credentials**: Change default admin credentials before deploying to production.

6. **Database Credentials**: Use strong database passwords and restrict database access.

7. **CORS**: Configure CORS appropriately for your frontend application.

8. **Rate Limiting**: Consider implementing rate limiting for authentication endpoints.

## Development Notes

- **Email Uniqueness**: Email uniqueness is enforced across both `Student` and `UserAccount` entities
- **DTO Pattern**: Requests use `Create/Update...Request` naming, responses use `...Response`
- **Location Headers**: Create operations return `201 Created` with a `Location` header pointing to the new resource
- **No Body on Create**: Create endpoints return only the Location header (no body) to prevent reflection attacks
- **Docker Compose**: Spring Boot Docker Compose support is disabled in containerized environments to avoid conflicts

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

## New Relic

<details>
<summary>Click to open</summary>

This project ships with the New Relic Java agent wired into the Gradle build and Jib image.
The agent is loaded automatically when a license key is present.
Configuration lives in `src/main/newrelic/newrelic.yml` and supports environment overrides.

### 1) Get your New Relic license key

- In New Relic, go to **API keys** and copy your **License key**.
- Keep it out of git; use environment variables or `.env`.

### 2) Provide environment variables

Required:
- `NEW_RELIC_LICENSE_KEY` (your license key)

Optional:
- `NEW_RELIC_APP_NAME` (defaults to `jib-pilot`)

Example `.env`:

```bash
NEW_RELIC_LICENSE_KEY=YOUR_LICENSE_KEY
NEW_RELIC_APP_NAME=jib-pilot
```

### 3) Run locally (bootRun)

The `bootRun` task loads the agent only when `NEW_RELIC_LICENSE_KEY` is set.

```bash
export NEW_RELIC_LICENSE_KEY=YOUR_LICENSE_KEY
export NEW_RELIC_APP_NAME=jib-pilot
./gradlew bootRun
```

You should see the agent initialize in the logs and the app appear in New Relic APM.

### 4) Run with Docker/Jib

The Jib build copies the New Relic agent into the image and starts the JVM with
`-javaagent:/newrelic/newrelic.jar`.

```bash
./gradlew jibDockerBuild
docker compose up -d
```

Make sure `NEW_RELIC_LICENSE_KEY` is set in your environment or `.env` file before running Compose.

### 5) Customize agent settings (optional)

Edit `src/main/newrelic/newrelic.yml` to adjust:
- `app_name` (static default)
- log level
- distributed tracing

### Gradle wiring (what this project does)

- Adds a `newrelicAgent` configuration to pull the official agent JAR.
- `prepareNewRelicAgent` copies the agent to `build/newrelic/newrelic.jar` and overlays `src/main/newrelic/newrelic.yml`.
- `bootRun` attaches `-javaagent` only when `NEW_RELIC_LICENSE_KEY` is present.
- Jib tasks depend on the agent prep and run the container with `-javaagent:/newrelic/newrelic.jar`.

</details>

## License

[Add your license information here]

## Contributing

[Add contribution guidelines here]

## Support

[Add support information here]
