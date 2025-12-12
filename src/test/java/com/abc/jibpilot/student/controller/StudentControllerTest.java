package com.abc.jibpilot.student.controller;

import com.abc.jibpilot.auth.filter.JwtAuthenticationFilter;
import com.abc.jibpilot.auth.service.JwtService;
import com.abc.jibpilot.course.dto.CourseSummaryResponse;
import com.abc.jibpilot.ratelimit.RateLimitingFilter;
import com.abc.jibpilot.security.SecurityGuard;
import com.abc.jibpilot.student.dto.CreateStudentRequest;
import com.abc.jibpilot.student.dto.StudentResponse;
import com.abc.jibpilot.student.dto.UpdateStudentRequest;
import com.abc.jibpilot.student.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private SecurityGuard securityGuard;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RateLimitingFilter rateLimitingFilter;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createStudent_returnsCreatedWithLocationHeader() throws Exception {
        CreateStudentRequest request = new CreateStudentRequest("Jane", "Doe", "jane@example.com", Set.of());
        StudentResponse response = new StudentResponse(5L, request.firstName(), request.lastName(), request.email(), Set.of());
        when(studentService.createStudent(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/students/5"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getStudent_returnsStudent() throws Exception {
        when(securityGuard.canAccessStudent(1L)).thenReturn(true);
        StudentResponse response = new StudentResponse(
                1L,
                "John",
                "Smith",
                "john@example.com",
                Set.of(new CourseSummaryResponse(10L, "CS101", "Intro to CS"))
        );
        when(studentService.getStudent(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/students/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllStudents_returnsList() throws Exception {
        List<StudentResponse> responses = List.of(
                new StudentResponse(1L, "Alice", "Smith", "alice@example.com", Set.of()),
                new StudentResponse(2L, "Bob", "Jones", "bob@example.com", Set.of())
        );
        when(studentService.getAllStudents()).thenReturn(responses);

        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responses)));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void updateStudent_returnsUpdatedStudent() throws Exception {
        when(securityGuard.canAccessStudent(3L)).thenReturn(true);
        UpdateStudentRequest request = new UpdateStudentRequest("Jane", "Doe", "jane@example.com", Set.of(1L, 2L));
        StudentResponse response = new StudentResponse(3L, request.firstName(), request.lastName(), request.email(), Set.of());
        when(studentService.updateStudent(3L, request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/students/{id}", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void deleteStudent_returnsNoContent() throws Exception {
        when(securityGuard.canAccessStudent(4L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/students/{id}", 4L))
                .andExpect(status().isNoContent());

        verify(studentService).deleteStudent(4L);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void enrollInCourse_returnsUpdatedStudent() throws Exception {
        when(securityGuard.canAccessStudent(anyLong())).thenReturn(true);
        StudentResponse response = new StudentResponse(
                6L,
                "Eve",
                "Adams",
                "eve@example.com",
                Set.of(new CourseSummaryResponse(11L, "CS102", "Algorithms"))
        );
        when(studentService.enrollStudentInCourse(6L, 11L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/students/{studentId}/courses/{courseId}", 6L, 11L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void dropCourse_returnsUpdatedStudent() throws Exception {
        when(securityGuard.canAccessStudent(anyLong())).thenReturn(true);
        StudentResponse response = new StudentResponse(
                6L,
                "Eve",
                "Adams",
                "eve@example.com",
                Set.of()
        );
        when(studentService.removeStudentFromCourse(6L, 11L)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/students/{studentId}/courses/{courseId}", 6L, 11L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
}
