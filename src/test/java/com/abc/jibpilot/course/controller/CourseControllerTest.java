package com.abc.jibpilot.course.controller;

import com.abc.jibpilot.course.dto.CourseResponse;
import com.abc.jibpilot.course.dto.CreateCourseRequest;
import com.abc.jibpilot.course.dto.UpdateCourseRequest;
import com.abc.jibpilot.course.service.CourseService;
import com.abc.jibpilot.student.dto.StudentResponse;
import com.abc.jibpilot.student.service.StudentService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.abc.jibpilot.auth.filter.JwtAuthenticationFilter;
import com.abc.jibpilot.auth.service.JwtService;
import com.abc.jibpilot.ratelimit.RateLimitingFilter;
import com.abc.jibpilot.config.JacksonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("courseControllerTest")
@WebMvcTest(controllers = CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import(JacksonConfig.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RateLimitingFilter rateLimitingFilter;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_returnsCreatedWithLocationHeader() throws Exception {
        System.out.println("createCourse_returnsCreatedWithLocationHeader");
        CreateCourseRequest request = new CreateCourseRequest("CS101", "Intro to CS", "Basics");
        CourseResponse response = new CourseResponse(1L, request.code(), request.title(), request.description(), Set.of());
        when(courseService.createCourse(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/courses/1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCourse_returnsCourse() throws Exception {
        CourseResponse response = new CourseResponse(7L, "CS201", "Data Structures", "Learn data structures", Set.of(2L, 3L));
        when(courseService.getCourse(7L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/courses/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCourses_returnsList() throws Exception {
        List<CourseResponse> responses = List.of(
                new CourseResponse(1L, "CS101", "Intro to CS", "Basics", Set.of(10L)),
                new CourseResponse(2L, "CS102", "Algorithms", "Design algorithms", Set.of())
        );
        when(courseService.getAllCourses()).thenReturn(responses);

        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonMapper.writeValueAsString(responses)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCourse_returnsUpdatedCourse() throws Exception {
        UpdateCourseRequest request = new UpdateCourseRequest("CS101", "Intro to CS", "Updated description");
        CourseResponse response = new CourseResponse(1L, request.code(), request.title(), request.description(), Set.of());
        when(courseService.updateCourse(1L, request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/courses/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCourse_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/courses/{id}", 5L))
                .andExpect(status().isNoContent());

        verify(courseService).deleteCourse(5L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStudentsForCourse_returnsStudentList() throws Exception {
        List<StudentResponse> responses = List.of(
                new StudentResponse(1L, "Alice", "Smith", "alice@example.com", Set.of()),
                new StudentResponse(2L, "Bob", "Jones", "bob@example.com", Set.of())
        );
        when(studentService.getStudentsByCourse(8L)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/courses/{id}/students", 8L))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonMapper.writeValueAsString(responses)));
    }
}
