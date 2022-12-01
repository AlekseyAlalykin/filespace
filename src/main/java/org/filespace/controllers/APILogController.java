package org.filespace.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
public class APILogController {

    @GetMapping
    public String getLogs(){

        return "temp";
    }

    @GetMapping("/{id}")
    public String getLog(@PathVariable String id){

        return "temp";
    }
}
