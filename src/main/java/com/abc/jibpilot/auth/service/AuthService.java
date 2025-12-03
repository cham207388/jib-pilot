package com.abc.jibpilot.auth.service;

import com.abc.jibpilot.auth.dto.AuthResponse;
import com.abc.jibpilot.auth.dto.LoginRequest;
import com.abc.jibpilot.auth.dto.RegisterStudentRequest;
import com.abc.jibpilot.auth.entity.UserAccount;
import com.abc.jibpilot.auth.model.AppUserDetails;
import com.abc.jibpilot.auth.model.Role;
import com.abc.jibpilot.auth.repository.UserRepository;
import com.abc.jibpilot.student.entity.Student;
import com.abc.jibpilot.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse registerStudent(RegisterStudentRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(CONFLICT, "Email already in use");
        }
        studentRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new ResponseStatusException(CONFLICT, "Email already in use");
        });

        Student student = Student.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();

        Student savedStudent = studentRepository.save(student);

        UserAccount user = UserAccount.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.STUDENT)
                .student(savedStudent)
                .build();
        savedStudent.setUserAccount(user);

        UserAccount savedUser = userRepository.save(user);

        AppUserDetails userDetails = new AppUserDetails(
                savedUser.getId(),
                savedStudent.getId(),
                savedUser.getEmail(),
                savedUser.getPassword(),
                savedUser.getRole()
        );

        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, "Bearer", savedUser.getRole(), savedStudent.getId());
    }

    public AuthResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());
        var authentication = authenticationManager.authenticate(authToken);
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, "Bearer", userDetails.getRole(), userDetails.getStudentId());
    }
}
