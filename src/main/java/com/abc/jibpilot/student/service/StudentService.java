package com.abc.jibpilot.student.service;

import com.abc.jibpilot.student.dto.CreateStudentRequest;
import com.abc.jibpilot.student.dto.StudentResponse;
import com.abc.jibpilot.student.dto.UpdateStudentRequest;

import java.util.List;

public interface StudentService {
    StudentResponse createStudent(CreateStudentRequest request);

    StudentResponse getStudent(Long id);

    List<StudentResponse> getAllStudents();

    StudentResponse updateStudent(Long id, UpdateStudentRequest request);

    void deleteStudent(Long id);

    StudentResponse enrollStudentInCourse(Long studentId, Long courseId);

    StudentResponse removeStudentFromCourse(Long studentId, Long courseId);

    List<StudentResponse> getStudentsByCourse(Long courseId);
}
