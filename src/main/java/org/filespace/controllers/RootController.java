package org.filespace.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class RootController {
    @GetMapping("/test")
    public String testMethod(){
        return "file_upload_template";
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
