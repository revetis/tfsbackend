package com.example.tfs.utils;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.example.apps.auths.securities.CustomUserDetails;

@Component
public class SecurityUtils {

    public static String getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .map(authentication -> {
                    if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
                        return springSecurityUser.getUsername();
                    } else if (authentication.getPrincipal() instanceof String s) {
                        return s;
                    }
                    return null;
                }).orElseThrow(() -> new IllegalStateException("User not authenticated"));
    }

    public static Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        }

        throw new IllegalStateException("User not authenticated or invalid principal type");
    }
}