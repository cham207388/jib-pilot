package com.abc.jibpilot.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BulkCreateCourseRequest(
        @NotEmpty(message = "At least one course is required")
        @Size(max = 100, message = "Cannot create more than 100 courses at once")
        @Valid
        List<CreateCourseRequest> courses
) {
}
