package com.abc.jibpilot.actuator;

import com.abc.jibpilot.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Endpoint(id = "userNumbers")
@RequiredArgsConstructor
public class UserNumbersEndpoint {

    private final UserRepository userRepository;

    @ReadOperation
    public Map<String, Long> getUserCount() {
        long count = userRepository.count();
        return Map.of("user-count", count);
    }
}

