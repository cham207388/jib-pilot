package com.abc.jibpilot.auth.dto;

import com.abc.jibpilot.auth.model.Role;

public record AuthResponse(
        String token,
        String tokenType,
        Role role,
        Long studentId
) {}
