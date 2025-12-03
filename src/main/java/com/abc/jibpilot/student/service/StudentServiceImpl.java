package com.abc.jibpilot.student.service;

import com.abc.jibpilot.student.dto.StudentRequestDto;
import com.abc.jibpilot.student.dto.StudentResponseDto;
import com.abc.jibpilot.student.entity.Student;
import com.abc.jibpilot.student.exception.StudentNotFoundException;
import com.abc.jibpilot.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    public StudentResponseDto createStudent(StudentRequestDto request) {
        ensureEmailIsUnique(request.email(), null);

        Student student = Student.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();

        return toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDto getStudent(Long id) {
        return studentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponseDto> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public StudentResponseDto updateStudent(Long id, StudentRequestDto request) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));

        ensureEmailIsUnique(request.email(), id);

        existing.setFirstName(request.firstName());
        existing.setLastName(request.lastName());
        existing.setEmail(request.email());

        return toResponse(studentRepository.save(existing));
    }

    @Override
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new StudentNotFoundException(id);
        }
        studentRepository.deleteById(id);
    }

    private StudentResponseDto toResponse(Student student) {
        return new StudentResponseDto(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail()
        );
    }

    private void ensureEmailIsUnique(String email, Long currentId) {
        studentRepository.findByEmail(email).ifPresent(existing -> {
            boolean isDifferentRecord = currentId == null || !existing.getId().equals(currentId);
            if (isDifferentRecord) {
                throw new ResponseStatusException(CONFLICT, "Email already in use");
            }
        });
    }
}
