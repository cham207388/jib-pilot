package com.abc.jibpilot.course.service;

import com.abc.jibpilot.course.dto.CourseRequestDto;
import com.abc.jibpilot.course.dto.CourseResponseDto;

import java.util.List;

public interface CourseService {
    CourseResponseDto createCourse(CourseRequestDto request);

    CourseResponseDto getCourse(Long id);

    List<CourseResponseDto> getAllCourses();

    CourseResponseDto updateCourse(Long id, CourseRequestDto request);

    void deleteCourse(Long id);
}
