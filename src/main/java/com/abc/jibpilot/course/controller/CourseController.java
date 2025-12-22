package com.abc.jibpilot.course.controller;

import com.abc.jibpilot.auth.model.AppUserDetails;
import com.abc.jibpilot.auth.model.Role;
import com.abc.jibpilot.course.dto.BulkCreateCourseRequest;
import com.abc.jibpilot.course.dto.CourseResponse;
import com.abc.jibpilot.course.dto.CreateCourseRequest;
import com.abc.jibpilot.course.dto.UpdateCourseRequest;
import com.abc.jibpilot.course.service.CourseService;
import com.abc.jibpilot.student.dto.StudentResponse;
import com.abc.jibpilot.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

    private final CourseService courseService;
    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        log.info("Creating course: {}", request);
        CourseResponse createdCourse = courseService.createCourse(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/courses/{id}")
                .buildAndExpand(createdCourse.id())
                .encode()
                .toUri();
        // Return 201 with Location header only; avoid returning the created entity to prevent reflection
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseResponse>> bulkCreateCourses(@Valid @RequestBody BulkCreateCourseRequest request) {
        log.info("Bulk creating {} courses", request.courses().size());
        List<CourseResponse> createdCourses = courseService.bulkCreateCourses(request.courses());
        return ResponseEntity.ok(createdCourses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourse(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<List<CourseResponse>> searchCourses(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Searching courses with query: '{}', limit: {}", q, limit);
        return ResponseEntity.ok(courseService.searchCourses(q, limit));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateCourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponse>> getStudentsForCourse(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentsByCourse(id));
    }

    @PostMapping("/{courseId}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponse> enrollInCourse(@PathVariable Long courseId) {
        Long studentId = getCurrentStudentId();
        if (studentId == null) {
            return ResponseEntity.badRequest().build();
        }
        log.info("Student {} enrolling in course {}", studentId, courseId);
        return ResponseEntity.ok(studentService.enrollStudentInCourse(studentId, courseId));
    }

    @DeleteMapping("/{courseId}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponse> dropCourse(@PathVariable Long courseId) {
        Long studentId = getCurrentStudentId();
        if (studentId == null) {
            return ResponseEntity.badRequest().build();
        }
        log.info("Student {} dropping course {}", studentId, courseId);
        return ResponseEntity.ok(studentService.removeStudentFromCourse(studentId, courseId));
    }

    /**
     * Gets the current authenticated student's ID from the security context.
     * Returns null if the user is not a student or not authenticated.
     */
    private Long getCurrentStudentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails user)) {
            return null;
        }
        if (user.getRole() != Role.STUDENT) {
            return null;
        }
        return user.getStudentId();
    }
}
