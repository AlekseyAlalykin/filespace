package org.filespace.controllers;

import org.filespace.config.Response;
import org.filespace.model.entities.User;
import org.filespace.repositories.UserRepository;
import org.filespace.security.SecurityUtil;
import org.filespace.security.SessionManager;
import org.filespace.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@CrossOrigin
@Controller
@RequestMapping("/")
public class RootController {

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    SessionManager sessionManager;

    @Autowired
    SecurityUtil securityUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private Environment env;


    @GetMapping("/api/test")
    public ResponseEntity testMethod(){
        //sessionManager.test(securityUtil.getCurrentUser());
        System.out.println("__________TEST__________");
        System.out.println(userRepository.existsByUsername("test"));
        //System.out.println(env.getProperty("server.ssl.key-store"));
        System.out.println("__________TEST-END__________");


        return ResponseEntity.status(HttpStatus.OK).body(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }



}
