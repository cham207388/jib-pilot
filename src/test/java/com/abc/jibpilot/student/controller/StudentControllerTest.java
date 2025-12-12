package com.abc.jibpilot.student.controller;

import com.abc.jibpilot.student.dto.CreateStudentRequest;
import com.abc.jibpilot.student.dto.StudentResponse;
import com.abc.jibpilot.student.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    StudentService studentService;

    @BeforeEach
    void setUp() {
        StudentController controller = new StudentController(studentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createStudent_returns201WithLocation() throws Exception {
        CreateStudentRequest req = new CreateStudentRequest("John", "Doe", "john@example.com", Set.of());
        StudentResponse created = new StudentResponse(123L, "John", "Doe", "john@example.com", Set.of());
        when(studentService.createStudent(any(CreateStudentRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/students/123")));
    }

    @Test
    void getStudent_returns200WithBody() throws Exception {
        StudentResponse resp = new StudentResponse(5L, "A", "B", "a@b.com", Set.of());
        when(studentService.getStudent(5L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/students/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("a@b.com"));
    }
}
