package com.abc.jibpilot.student.service;

import com.abc.jibpilot.course.dto.CourseSummaryResponse;
import com.abc.jibpilot.course.entity.Course;
import com.abc.jibpilot.course.exception.CourseNotFoundException;
import com.abc.jibpilot.course.repository.CourseRepository;
import com.abc.jibpilot.auth.repository.UserRepository;
import com.abc.jibpilot.student.dto.CreateStudentRequest;
import com.abc.jibpilot.student.dto.StudentResponse;
import com.abc.jibpilot.student.dto.UpdateStudentRequest;
import com.abc.jibpilot.student.entity.Student;
import com.abc.jibpilot.student.exception.StudentNotFoundException;
import com.abc.jibpilot.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public StudentResponse createStudent(CreateStudentRequest request) {
        ensureEmailIsUnique(request.email(), null);

        Student student = Student.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .courses(resolveCourses(request.courseIds()))
                .build();

        return toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudent(Long id) {
        return studentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public StudentResponse updateStudent(Long id, UpdateStudentRequest request) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));

        ensureEmailIsUnique(request.email(), id);

        existing.setFirstName(request.firstName());
        existing.setLastName(request.lastName());
        existing.setEmail(request.email());
        if (request.courseIds() != null) {
            existing.setCourses(resolveCourses(request.courseIds()));
        }

        return toResponse(studentRepository.save(existing));
    }

    @Override
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));

        student.getCourses().forEach(course -> course.getStudents().remove(student));

        if (student.getUserAccount() != null) {
            userRepository.delete(student.getUserAccount());
        }

        studentRepository.delete(student);
    }

    @Override
    public StudentResponse enrollStudentInCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        student.getCourses().add(course);
        course.getStudents().add(student);

        return toResponse(studentRepository.save(student));
    }

    @Override
    public StudentResponse removeStudentFromCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        student.getCourses().remove(course);
        course.getStudents().remove(student);

        return toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        return course.getStudents()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                toCourseSummaries(student.getCourses())
        );
    }

    private void ensureEmailIsUnique(String email, Long currentId) {
        studentRepository.findByEmail(email).ifPresent(existing -> {
            boolean isDifferentRecord = currentId == null || !existing.getId().equals(currentId);
            if (isDifferentRecord) {
                throw new ResponseStatusException(CONFLICT, "Email already in use");
            }
        });

        userRepository.findByEmail(email).ifPresent(user -> {
            Long linkedStudentId = user.getStudent() != null ? user.getStudent().getId() : null;
            boolean isDifferentRecord = currentId == null || !currentId.equals(linkedStudentId);
            if (isDifferentRecord) {
                throw new ResponseStatusException(CONFLICT, "Email already in use");
            }
        });
    }

    private Set<Course> resolveCourses(Set<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Course> courses = courseRepository.findAllById(courseIds);
        if (courses.size() != courseIds.size()) {
            Set<Long> foundIds = courses.stream()
                    .map(Course::getId)
                    .collect(Collectors.toSet());
            Long missingId = courseIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElse(null);
            throw new CourseNotFoundException(missingId);
        }

        return new HashSet<>(courses);
    }

    private Set<CourseSummaryResponse> toCourseSummaries(Set<Course> courses) {
        return courses.stream()
                .map(course -> new CourseSummaryResponse(course.getId(), course.getCode(), course.getTitle()))
                .collect(Collectors.toSet());
    }
}
