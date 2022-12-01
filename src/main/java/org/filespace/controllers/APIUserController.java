package org.filespace.controllers;

import org.filespace.model.User;
import org.filespace.repositories.UserRepository;
import org.filespace.services.IntegratedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users")
public class APIUserController {

    @Autowired
    IntegratedService integratedService;

    @PostMapping
    public ResponseEntity postUser(@RequestParam("username") String username,
                                   @RequestParam("password") String password,
                                   @RequestParam("email") String email){



        try {
            integratedService.registerUser(username, password, email);

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }


        return ResponseEntity.status(HttpStatus.CREATED)
                .body("New user successfully created");
    }

    @GetMapping("/{id}")
    public ResponseEntity getUser(@PathVariable String id){
        User user;

        try {
            user = integratedService.getUserById(id);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No user with such id found");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity patchUser(@PathVariable String id){

        return null;
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable String id){

        return "temp";
    }
}
