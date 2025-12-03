package com.abc.jibpilot.course.service;

import com.abc.jibpilot.course.dto.CourseResponse;
import com.abc.jibpilot.course.dto.CreateCourseRequest;
import com.abc.jibpilot.course.dto.UpdateCourseRequest;
import com.abc.jibpilot.course.entity.Course;
import com.abc.jibpilot.course.exception.CourseNotFoundException;
import com.abc.jibpilot.course.repository.CourseRepository;
import com.abc.jibpilot.student.entity.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public CourseResponse createCourse(CreateCourseRequest request) {
        ensureCodeIsUnique(request.code(), null);

        Course course = Course.builder()
                .code(request.code())
                .title(request.title())
                .description(request.description())
                .build();

        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long id) {
        return courseRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));

        ensureCodeIsUnique(request.code(), id);

        course.setCode(request.code());
        course.setTitle(request.title());
        course.setDescription(request.description());

        return toResponse(courseRepository.save(course));
    }

    @Override
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));

        // Detach from students to keep the join table clean.
        for (Student student : course.getStudents()) {
            student.getCourses().remove(course);
        }

        courseRepository.delete(course);
    }

    private void ensureCodeIsUnique(String code, Long currentId) {
        courseRepository.findByCode(code).ifPresent(existing -> {
            boolean isDifferentRecord = currentId == null || !existing.getId().equals(currentId);
            if (isDifferentRecord) {
                throw new ResponseStatusException(CONFLICT, "Course code already in use");
            }
        });
    }

    private CourseResponse toResponse(Course course) {
        Set<Long> studentIds = course.getStudents()
                .stream()
                .map(Student::getId)
                .collect(Collectors.toSet());

        return new CourseResponse(
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getDescription(),
                studentIds
        );
    }
}
