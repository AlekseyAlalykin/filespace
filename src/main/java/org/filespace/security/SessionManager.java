package org.filespace.security;

import org.filespace.model.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;


@Service
public class SessionManager {

    @Autowired
    private SessionRegistry sessionRegistry;

    public void closeAllUserSessions(User user){
        for (SessionInformation sessionInformation:
                sessionRegistry.getAllSessions(UserDetailsServiceImpl.fromUser(user), false)){
            sessionInformation.expireNow();
        }
    }
}
