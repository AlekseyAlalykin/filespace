package org.filespace.controllers;

import org.filespace.model.entities.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
@RequestMapping("/")
public class RootController {
    @PostMapping("/api/test")
    public ResponseEntity testMethod(@RequestBody User user){
        System.out.println(user.getUsername());
        System.out.println(user.getEmail());
        System.out.println(user.getPassword());
        return ResponseEntity.status(HttpStatus.OK).body("Received");
    }

    @RequestMapping("/login")
    public String getLoginPage(){
        return "login";
    }

    @GetMapping("/registration")
    public String getRegistrationPage(){
        return "registration";
    }

}
