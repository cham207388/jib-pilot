package com.abc.jibpilot.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCourseRequest(
        @NotBlank(message = "Course code is required")
        String code,

        @NotBlank(message = "Title is required")
        String title,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description
) {
}
