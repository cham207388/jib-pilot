package com.abc.jibpilot.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RateLimitingConfig {

    @Value("${app.rate-limiting.enabled:true}")
    private boolean rateLimitingEnabled;

    @Value("${app.rate-limiting.auth.requests-per-minute:5}")
    private int authRequestsPerMinute;

    @Value("${app.rate-limiting.public.requests-per-minute:100}")
    private int publicRequestsPerMinute;

    @Value("${app.rate-limiting.authenticated.requests-per-minute:1000}")
    private int authenticatedRequestsPerMinute;

    public boolean isRateLimitingEnabled() {
        return rateLimitingEnabled;
    }

    public int getAuthRequestsPerMinute() {
        return authRequestsPerMinute;
    }

    public int getPublicRequestsPerMinute() {
        return publicRequestsPerMinute;
    }

    public int getAuthenticatedRequestsPerMinute() {
        return authenticatedRequestsPerMinute;
    }
}

