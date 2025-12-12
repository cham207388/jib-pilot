package com.abc.jibpilot.course.controller;

import com.abc.jibpilot.course.dto.CourseResponse;
import com.abc.jibpilot.course.dto.CreateCourseRequest;
import com.abc.jibpilot.course.service.CourseService;
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
class CourseControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    CourseService courseService;

    @Mock
    StudentService studentService;

    @BeforeEach
    void setUp() {
        CourseController controller = new CourseController(courseService, studentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createCourse_returns201WithLocation() throws Exception {
        CreateCourseRequest req = new CreateCourseRequest("CS101", "Intro", "desc");
        CourseResponse created = new CourseResponse(42L, "CS101", "Intro", "desc", Set.of());
        when(courseService.createCourse(any(CreateCourseRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/courses/42")));
    }

    @Test
    void getCourse_returns200WithBody() throws Exception {
        CourseResponse resp = new CourseResponse(7L, "MATH101", "Math", "desc", Set.of(1L,2L));
        when(courseService.getCourse(7L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/courses/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.code").value("MATH101"));
    }
}
