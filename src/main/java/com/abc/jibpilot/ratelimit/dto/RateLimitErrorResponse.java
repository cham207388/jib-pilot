package com.abc.jibpilot.ratelimit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RateLimitErrorResponse(
        String error,
        String message,
        Long retryAfter
) {
    public static RateLimitErrorResponse tooManyRequests(long retryAfterSeconds) {
        return new RateLimitErrorResponse(
                "Too Many Requests",
                "Rate limit exceeded. Please try again later.",
                retryAfterSeconds
        );
    }
}

