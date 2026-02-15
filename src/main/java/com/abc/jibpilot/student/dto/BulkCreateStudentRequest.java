package com.abc.jibpilot.student.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BulkCreateStudentRequest(
        @NotEmpty(message = "At least one student is required")
        @Size(max = 100, message = "Cannot create more than 100 students at once")
        @Valid
        List<CreateStudentRequest> students
) {
}
