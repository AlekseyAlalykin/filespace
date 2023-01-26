package org.filespace.controllers;

import org.filespace.model.entities.User;
import org.filespace.security.SecurityUtil;
import org.filespace.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/users")
public class UserControllerAPI {

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity createUser(@RequestParam("username") String username,
                                     @RequestParam("password") String password,
                                     @RequestParam("email") String email){

        User user;

        try {
            user = userService.registerUser(username, password, email);

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity getUser(@PathVariable String id){
        User user;

        try {
            Long lId = Long.parseLong(id);

            user = userService.getUserById(lId);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No user with such id found");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity updateUser(@PathVariable String id,
                                     @RequestParam(value = "username",required = false) String username,
                                     @RequestParam(value = "password",required = false) String password,
                                     @RequestParam(value = "email",required = false) String email){

        try {
            Long lId = Long.parseLong(id);

            userService.updateUser(securityUtil.getCurrentUser(), lId, username, password, email);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable String id){

        try {
            Long lId = Long.parseLong(id);

            userService.deleteUser(securityUtil.getCurrentUser(), lId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

}
