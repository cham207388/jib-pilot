package com.abc.jibpilot.student.service;

import com.abc.jibpilot.course.dto.CourseSummaryDto;
import com.abc.jibpilot.course.entity.Course;
import com.abc.jibpilot.course.exception.CourseNotFoundException;
import com.abc.jibpilot.course.repository.CourseRepository;
import com.abc.jibpilot.auth.repository.UserRepository;
import com.abc.jibpilot.student.dto.StudentRequestDto;
import com.abc.jibpilot.student.dto.StudentResponseDto;
import com.abc.jibpilot.student.entity.Student;
import com.abc.jibpilot.student.exception.StudentNotFoundException;
import com.abc.jibpilot.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Course course1;
    private Course course2;

    @BeforeEach
    void setUp() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        course1 = Course.builder().id(1L).code("CS101").title("Intro").build();
        course2 = Course.builder().id(2L).code("MATH201").title("Algebra").build();
    }

    @Test
    void createStudent_withCourses_resolvesCoursesAndSaves() {
        StudentRequestDto request = new StudentRequestDto("John", "Doe", "john@example.com", Set.of(1L, 2L));
        when(courseRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(course1, course2));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });

        StudentResponseDto response = studentService.createStudent(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.courses())
                .extracting(CourseSummaryDto::id)
                .containsExactlyInAnyOrder(1L, 2L);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void createStudent_conflictEmail_throwsResponseStatusException() {
        when(studentRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(new Student()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                studentService.createStudent(new StudentRequestDto("John", "Doe", "dup@example.com", null)));

        assertThat(ex.getStatusCode()).isEqualTo(CONFLICT);
    }

    @Test
    void updateStudent_notFound_throws() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () ->
                studentService.updateStudent(99L, new StudentRequestDto("A", "B", "a@b.com", null)));
    }

    @Test
    void enrollStudentInCourse_addsRelationship() {
        Student student = Student.builder()
                .id(5L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .courses(new HashSet<>())
                .build();
        when(studentRepository.findById(5L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course2));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentResponseDto response = studentService.enrollStudentInCourse(5L, 2L);

        assertThat(response.courses()).extracting(CourseSummaryDto::id).containsExactly(2L);
        assertThat(course2.getStudents()).contains(student);
    }

    @Test
    void removeStudentFromCourse_missingCourse_throws() {
        Student student = Student.builder().id(1L).courses(new HashSet<>()).build();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () ->
                studentService.removeStudentFromCourse(1L, 3L));
    }

    @Test
    void getStudentsByCourse_returnsMappedStudents() {
        Student student = Student.builder()
                .id(1L)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .courses(new HashSet<>())
                .build();
        course1.setStudents(Set.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));

        List<StudentResponseDto> result = studentService.getStudentsByCourse(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("test@example.com");
    }

    @Test
    void createStudent_missingCourseId_throwsNotFoundWithMissingId() {
        StudentRequestDto request = new StudentRequestDto("John", "Doe", "john@example.com", Set.of(1L, 99L));
        when(courseRepository.findAllById(Set.of(1L, 99L))).thenReturn(List.of(course1));

        CourseNotFoundException ex = assertThrows(CourseNotFoundException.class, () -> studentService.createStudent(request));

        assertThat(ex.getMessage()).contains("99");
    }
}
