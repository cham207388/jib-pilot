package com.abc.jibpilot.course.dto;

import java.util.Set;

public record CourseResponseDto(
        Long id,
        String code,
        String title,
        String description,
        Set<Long> studentIds
) {
}
