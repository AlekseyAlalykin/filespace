package org.filespace.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    public static String getCurrentUserUsername(){
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }
}
