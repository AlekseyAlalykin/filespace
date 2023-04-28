package org.filespace.controllers;

import org.filespace.config.Response;
import org.filespace.model.entities.User;
import org.filespace.model.intermediate.UserInfo;
import org.filespace.security.SecurityUtil;
import org.filespace.security.SessionManager;
import org.filespace.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserControllerAPI {

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionManager sessionManager;

    @PostMapping(path = "/users", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ResponseEntity createUserConsumesURLEncoded(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email){

        User user;

        try {
            user = userService.registerUser(username, password, email);

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(user);
    }

    @PostMapping(path = "/users", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity createUser(@RequestBody User user){

        try {
            if (user.getUsername() == null)
                throw new Exception("No username specified");
            if (user.getEmail() == null)
                throw new Exception("No email specified");
            if (user.getPassword() == null)
                throw new Exception("No password specified");

            user = userService.registerUser(user.getUsername(), user.getPassword(), user.getEmail());

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(user);
    }

    @GetMapping(path = "/user/logout")
    public ResponseEntity logout(){
        sessionManager.closeAllUserSessions(securityUtil.getCurrentUser());
        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, HttpStatus.OK.getReasonPhrase()));
    }

    @GetMapping(path = "/user")
    public ResponseEntity getCurrentUser(){
        User user;
        try {
            user = userService.getCurrentUser();
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(user);
    }

    @GetMapping(path = "/users/{id}")
    public ResponseEntity getUser(@PathVariable String id){
        User user;

        try {
            Long lId = Long.parseLong(id);

            user = userService.getUserById(lId);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, "No user with such id found"));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(user);
    }

    @GetMapping(path = "/users")
    public ResponseEntity getUsers(
            @RequestParam(name = "q", required = true) String query,
            @RequestParam(name = "n", required = false) Integer limit ){
        List<UserInfo> list = null;
        try {
            if (limit == null)
                limit = 10;

            list = userService.getUsersList(query, limit);

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, "No user with such id found"));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(list);
    }

    @PatchMapping(path = "/users/{id}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ResponseEntity updateUserConsumesURLEncoded(
            @PathVariable String id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String email){

        try {
            Long lId = Long.parseLong(id);

            userService.updateUser(securityUtil.getCurrentUser(), lId, username, password, email);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Response.build(HttpStatus.OK,"OK"));
    }

    @PatchMapping(path = "/users/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateUser(@PathVariable String id, @RequestBody User user){

        try {
            Long lId = Long.parseLong(id);

            userService.updateUser(securityUtil.getCurrentUser(), lId, user.getUsername(), user.getPassword(), user.getEmail());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Response.build(HttpStatus.OK,"OK"));
    }

    @DeleteMapping(path = "/users/{id}")
    public ResponseEntity deleteUser(@PathVariable String id){

        try {
            Long lId = Long.parseLong(id);

            userService.deleteUser(securityUtil.getCurrentUser(), lId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Response.build(HttpStatus.OK,"OK"));
    }

    @GetMapping(path = "/user/token/{token}")
    public ResponseEntity confirmToken(@PathVariable String token){
        String message;
        try {
            message = userService.confirmToken(token);
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Response.build(HttpStatus.OK, message));
    }

}
