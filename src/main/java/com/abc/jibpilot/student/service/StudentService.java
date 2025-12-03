package com.abc.jibpilot.student.service;

import com.abc.jibpilot.student.dto.StudentRequestDto;
import com.abc.jibpilot.student.dto.StudentResponseDto;

import java.util.List;

public interface StudentService {
    StudentResponseDto createStudent(StudentRequestDto request);

    StudentResponseDto getStudent(Long id);

    List<StudentResponseDto> getAllStudents();

    StudentResponseDto updateStudent(Long id, StudentRequestDto request);

    void deleteStudent(Long id);

    StudentResponseDto enrollStudentInCourse(Long studentId, Long courseId);

    StudentResponseDto removeStudentFromCourse(Long studentId, Long courseId);

    List<StudentResponseDto> getStudentsByCourse(Long courseId);
}
