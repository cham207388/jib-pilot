package com.abc.jibpilot.course.service;

import com.abc.jibpilot.course.dto.CourseResponse;
import com.abc.jibpilot.course.dto.CreateCourseRequest;
import com.abc.jibpilot.course.dto.UpdateCourseRequest;

import java.util.List;

public interface CourseService {
    CourseResponse createCourse(CreateCourseRequest request);

    CourseResponse getCourse(Long id);

    List<CourseResponse> getAllCourses();

    CourseResponse updateCourse(Long id, UpdateCourseRequest request);

    void deleteCourse(Long id);
}
