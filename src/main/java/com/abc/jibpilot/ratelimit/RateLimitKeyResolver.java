package com.abc.jibpilot.ratelimit;

import com.abc.jibpilot.auth.model.AppUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class RateLimitKeyResolver {

    /**
     * Resolves the rate limit key for a request.
     * For authenticated users: returns user ID
     * For unauthenticated users: returns IP address
     *
     * @param request HTTP request
     * @return Rate limit key (user ID or IP address)
     */
    public String resolveKey(HttpServletRequest request) {
        // Try to get authenticated user first
        Optional<String> userId = getUserIdFromSecurityContext();
        if (userId.isPresent()) {
            return "user:" + userId.get();
        }

        // Fall back to IP address for unauthenticated requests
        return "ip:" + getClientIpAddress(request);
    }

    /**
     * Gets the user ID from Spring Security context if user is authenticated.
     */
    private Optional<String> getUserIdFromSecurityContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof AppUserDetails userDetails) {
                return Optional.of(String.valueOf(userDetails.getUserId()));
            }
        } catch (Exception e) {
            log.debug("Could not extract user ID from security context: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Extracts the client IP address from the request.
     * Handles proxies and load balancers by checking X-Forwarded-For header.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        // Fall back to remote address
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}

