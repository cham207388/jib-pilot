package com.abc.jibpilot.config;

import com.abc.jibpilot.auth.entity.UserAccount;
import com.abc.jibpilot.auth.model.Role;
import com.abc.jibpilot.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Bean
    @DependsOn("flywayMigrationInitializer")
    public CommandLineRunner seedAdmin() {
        log.info("Seeding admin user with email: {}", adminEmail);
        return args -> {
            if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
                return;
            }
            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }
            UserAccount admin = UserAccount.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
        };
    }
}
