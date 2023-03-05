package org.filespace.security;

import org.filespace.model.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.stereotype.Service;


@Service
public class SessionManager {

    @Autowired
    private SessionRegistry sessionRegistry;

    public void closeAllUserSessions(User user){
        for (SessionInformation sessionInformation:
                sessionRegistry.getAllSessions(UserDetailsServiceImpl.fromUser(user), false)){
            //System.out.println(sessionInformation.getSessionId() + " : " + sessionInformation + " : " + sessionInformation.getPrincipal().toString());
            sessionInformation.expireNow();
        }
    }

    public void updateUsernameForUserSessions(String oldUsername, String newUsername){


        for (Object principal: sessionRegistry.getAllPrincipals()){
            System.out.println(principal);
            UserDetailsImpl userDetails = (UserDetailsImpl)principal;
            if (userDetails.getUsername().equals(oldUsername))
                userDetails.setUsername(newUsername);
        }
    }

}
