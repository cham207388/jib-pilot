package com.abc.jibpilot.student.dto;

public record StudentResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email
) {
}
