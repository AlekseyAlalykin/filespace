package org.filespace.controllers;

import org.filespace.config.Response;
import org.filespace.model.entities.User;
import org.filespace.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
@RequestMapping("/")
public class RootController {

    @Autowired
    private SessionRegistry sessionRegistry;

    @GetMapping("/api/test")
    public ResponseEntity testMethod(){
        for (Object object:sessionRegistry.getAllPrincipals()) {
            UserDetailsImpl user = (UserDetailsImpl) (object);
        }

        return ResponseEntity.status(HttpStatus.OK).body(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }



}
