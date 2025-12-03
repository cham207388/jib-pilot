package com.abc.jibpilot.student.dto;

import com.abc.jibpilot.course.dto.CourseSummaryDto;

import java.util.Set;

public record StudentResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<CourseSummaryDto> courses
) {
}
