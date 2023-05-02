package org.filespace.security;

import org.filespace.model.entities.simplerelations.User;
import org.filespace.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtil {
    @Autowired
    private UserRepository userRepository;

    public String getCurrentUserUsername(){
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    public User getCurrentUser(){
        return userRepository.findUserByUsername(getCurrentUserUsername());
    }

    public Long getCurrentUserId(){
        return getCurrentUser().getId();
    }

    public Long getUserId(String userId){
        Long id;
        if (userId.equals("current"))
            id = getCurrentUserId();
        else
            id = Long.parseLong(userId);

        return id;
    }
}
