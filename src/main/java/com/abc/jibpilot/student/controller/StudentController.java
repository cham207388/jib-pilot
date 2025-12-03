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

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        StudentResponse created = studentService.createStudent(request);
        return ResponseEntity.created(URI.create("/api/v1/students/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityGuard.canAccessStudent(#id)")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudent(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityGuard.canAccessStudent(#id)")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateStudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityGuard.canAccessStudent(#id)")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{studentId}/courses/{courseId}")
    @PreAuthorize("@securityGuard.canAccessStudent(#studentId)")
    public ResponseEntity<StudentResponse> enrollInCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return ResponseEntity.ok(studentService.enrollStudentInCourse(studentId, courseId));
    }

    @DeleteMapping("/{studentId}/courses/{courseId}")
    @PreAuthorize("@securityGuard.canAccessStudent(#studentId)")
    public ResponseEntity<StudentResponse> dropCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return ResponseEntity.ok(studentService.removeStudentFromCourse(studentId, courseId));
    }
}
