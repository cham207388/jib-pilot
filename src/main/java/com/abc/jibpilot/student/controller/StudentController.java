package com.abc.jibpilot.student.controller;

import com.abc.jibpilot.student.dto.CreateStudentRequest;
import com.abc.jibpilot.student.dto.StudentResponse;
import com.abc.jibpilot.student.dto.UpdateStudentRequest;
import com.abc.jibpilot.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        StudentResponse createdStudent = studentService.createStudent(request);
        // Build the Location URI using an explicit context-path-based template to avoid reflecting request input
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/students/{id}")
                .buildAndExpand(createdStudent.id())
                .encode()
                .toUri();
        // Return 201 with Location header and no body to avoid reflecting user input
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityGuard.canAccessStudent(#id)")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Long id) {
        return ok(studentService.getStudent(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ok(studentService.getAllStudents());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityGuard.canAccessStudent(#id)")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateStudentRequest request) {
        return ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityGuard.canAccessStudent(#id)")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return noContent().build();
    }

    @PostMapping("/{studentId}/courses/{courseId}")
    @PreAuthorize("@securityGuard.canAccessStudent(#studentId)")
    public ResponseEntity<StudentResponse> enrollInCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return ok(studentService.enrollStudentInCourse(studentId, courseId));
    }

    @DeleteMapping("/{studentId}/courses/{courseId}")
    @PreAuthorize("@securityGuard.canAccessStudent(#studentId)")
    public ResponseEntity<StudentResponse> dropCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return ok(studentService.removeStudentFromCourse(studentId, courseId));
    }
}
