package com.abc.jibpilot.security;

import com.abc.jibpilot.auth.model.AppUserDetails;
import com.abc.jibpilot.auth.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityGuard {

    public boolean canAccessStudent(Long studentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails user)) {
            return false;
        }
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        return user.getRole() == Role.STUDENT && studentId != null && studentId.equals(user.getStudentId());
    }
}
