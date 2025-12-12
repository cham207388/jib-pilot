package com.abc.jibpilot.course.service;

import com.abc.jibpilot.course.dto.CourseResponse;
import com.abc.jibpilot.course.dto.CreateCourseRequest;
import com.abc.jibpilot.course.dto.UpdateCourseRequest;
import com.abc.jibpilot.course.entity.Course;
import com.abc.jibpilot.course.exception.CourseNotFoundException;
import com.abc.jibpilot.course.repository.CourseRepository;
import com.abc.jibpilot.student.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course;

    @BeforeEach
    void setUp() {
        course = Course.builder()
                .id(1L)
                .code("CS101")
                .title("Intro to CS")
                .description("Basics")
                .students(new HashSet<>())
                .build();
    }

    @Test
    void createCourse_conflictCodeThrows() {
        when(courseRepository.findByCode("CS101")).thenReturn(Optional.of(course));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                courseService.createCourse(new CreateCourseRequest("CS101", "Title", "Desc")));

        assertThat(ex.getStatusCode()).isEqualTo(CONFLICT);
    }

    @Test
    void getCourse_notFoundThrows() {
        when(courseRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () -> courseService.getCourse(5L));
    }

    @Test
    void updateCourse_updatesFields() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse response = courseService.updateCourse(1L, new UpdateCourseRequest("CS201", "Advanced", "More"));

        assertThat(response.code()).isEqualTo("CS201");
        assertThat(response.title()).isEqualTo("Advanced");
    }

    @Test
    void deleteCourse_detachesFromStudents() {
        Student student = Student.builder().id(2L).courses(new HashSet<>(Set.of(course))).build();
        course.setStudents(new HashSet<>(Set.of(student)));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        courseService.deleteCourse(1L);

        assertThat(student.getCourses()).isEmpty();
        verify(courseRepository).delete(course);
    }

    @Test
    void getAllCourses_returnsMappedDtos() {
        when(courseRepository.findAll()).thenReturn(List.of(course));

        List<CourseResponse> responses = courseService.getAllCourses();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).code()).isEqualTo("CS101");
    }
}
