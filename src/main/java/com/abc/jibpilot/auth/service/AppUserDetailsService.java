package com.abc.jibpilot.auth.service;

import com.abc.jibpilot.auth.entity.UserAccount;
import com.abc.jibpilot.auth.model.AppUserDetails;
import com.abc.jibpilot.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Long studentId = user.getStudent() != null ? user.getStudent().getId() : null;
        return new AppUserDetails(
                user.getId(),
                studentId,
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }
}
