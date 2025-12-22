package com.abc.jibpilot.ratelimit;

import com.abc.jibpilot.ratelimit.dto.RateLimitErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingConfig rateLimitingConfig;
    private final RateLimitKeyResolver keyResolver;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(RateLimitingConfig rateLimitingConfig, RateLimitKeyResolver keyResolver) {
        this.rateLimitingConfig = rateLimitingConfig;
        this.keyResolver = keyResolver;
        this.objectMapper = new ObjectMapper();
    }

    // In-memory storage for rate limit buckets
    private final ConcurrentMap<String, LocalBucket> authBuckets = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LocalBucket> publicBuckets = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LocalBucket> authenticatedBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, jakarta.servlet.ServletException {

        // Skip rate limiting if disabled
        if (!rateLimitingConfig.isRateLimitingEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Determine endpoint category
        EndpointCategory category = determineEndpointCategory(request.getRequestURI());
        if (category == EndpointCategory.NONE) {
            // No rate limiting for this endpoint
            filterChain.doFilter(request, response);
            return;
        }

        // Resolve rate limit key (IP or user ID)
        String key = keyResolver.resolveKey(request);

        try {
            // Get or create bucket for this key
            ConcurrentMap<String, LocalBucket> bucketMap = getBucketMap(category);
            LocalBucket bucket = bucketMap.computeIfAbsent(key, k -> createBucketForCategory(category));

            // Try to consume a token
            if (bucket.tryConsume(1)) {
                // Success - add rate limit headers and continue
                addRateLimitHeaders(response, bucket, category);
                filterChain.doFilter(request, response);
            } else {
                // Rate limit exceeded
                handleRateLimitExceeded(response, bucket, category);
            }
        } catch (Exception e) {
            log.error("Error in rate limiting filter", e);
            // On error, allow the request to proceed (fail open)
            filterChain.doFilter(request, response);
        }
    }

    private EndpointCategory determineEndpointCategory(String requestUri) {
        if (requestUri.startsWith("/api/v1/auth/")) {
            return EndpointCategory.AUTH;
        } else if (requestUri.startsWith("/actuator/") || 
                   requestUri.startsWith("/swagger-ui") || 
                   requestUri.startsWith("/v3/api-docs") ||
                   requestUri.startsWith("/swagger-resources") ||
                   requestUri.startsWith("/webjars")) {
            // Exclude actuator and swagger endpoints from rate limiting
            return EndpointCategory.NONE;
        } else if (requestUri.startsWith("/api/v1/")) {
            return EndpointCategory.AUTHENTICATED;
        }
        return EndpointCategory.NONE;
    }

    private ConcurrentMap<String, LocalBucket> getBucketMap(EndpointCategory category) {
        return switch (category) {
            case AUTH -> authBuckets;
            case PUBLIC -> publicBuckets;
            case AUTHENTICATED -> authenticatedBuckets;
            case NONE -> throw new IllegalArgumentException("Invalid category: NONE");
        };
    }

    private LocalBucket createBucketForCategory(EndpointCategory category) {
        int capacity = switch (category) {
            case AUTH -> rateLimitingConfig.getAuthRequestsPerMinute();
            case PUBLIC -> rateLimitingConfig.getPublicRequestsPerMinute();
            case AUTHENTICATED -> rateLimitingConfig.getAuthenticatedRequestsPerMinute();
            case NONE -> 0;
        };

        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void addRateLimitHeaders(HttpServletResponse response, LocalBucket bucket, EndpointCategory category) {
        int capacity = switch (category) {
            case AUTH -> rateLimitingConfig.getAuthRequestsPerMinute();
            case PUBLIC -> rateLimitingConfig.getPublicRequestsPerMinute();
            case AUTHENTICATED -> rateLimitingConfig.getAuthenticatedRequestsPerMinute();
            case NONE -> 0;
        };

        long availableTokens = bucket.getAvailableTokens();
        response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));
        response.setHeader("X-RateLimit-Reset", String.valueOf(Instant.now().plusSeconds(60).getEpochSecond()));
    }

    private void handleRateLimitExceeded(HttpServletResponse response, LocalBucket bucket, EndpointCategory category)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60");

        int capacity = switch (category) {
            case AUTH -> rateLimitingConfig.getAuthRequestsPerMinute();
            case PUBLIC -> rateLimitingConfig.getPublicRequestsPerMinute();
            case AUTHENTICATED -> rateLimitingConfig.getAuthenticatedRequestsPerMinute();
            case NONE -> 0;
        };

        response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Reset", String.valueOf(Instant.now().plusSeconds(60).getEpochSecond()));

        RateLimitErrorResponse errorResponse = RateLimitErrorResponse.tooManyRequests(60);
        objectMapper.writeValue(response.getWriter(), errorResponse);

        log.warn("Rate limit exceeded for category: {}", category);
    }

    private enum EndpointCategory {
        AUTH,
        PUBLIC,
        AUTHENTICATED,
        NONE
    }
}

