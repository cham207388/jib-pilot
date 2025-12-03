package com.abc.jibpilot.student.dto;

import com.abc.jibpilot.course.dto.CourseSummaryResponse;

import java.util.Set;

public record StudentResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<CourseSummaryResponse> courses
) {
}
