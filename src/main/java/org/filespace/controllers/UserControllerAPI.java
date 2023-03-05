package org.filespace.controllers;

import org.filespace.config.Response;
import org.filespace.model.entities.User;
import org.filespace.security.SecurityUtil;
import org.filespace.security.SessionManager;
import org.filespace.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserControllerAPI {

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionManager sessionManager;

    @PostMapping(headers = {"content-type=application/x-www-form-urlencoded"})
    public ResponseEntity createUser(@RequestParam("username") String username,
                                     @RequestParam("password") String password,
                                     @RequestParam("email") String email){

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

    @PostMapping(headers = {"content-type=application/json"})
    public ResponseEntity createUserFromJSON(@RequestBody User user){

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

    @GetMapping("/logout")
    public ResponseEntity logout(){
        sessionManager.closeAllUserSessions(securityUtil.getCurrentUser());
        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, HttpStatus.OK.getReasonPhrase()));
    }

    @GetMapping
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

    @GetMapping("/{id}")
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

    @PatchMapping(path = "/{id}", headers = {"content-type=application/x-www-form-urlencoded"})
    public ResponseEntity updateUser(@PathVariable String id,
                                     @RequestParam(value = "username",required = false) String username,
                                     @RequestParam(value = "password",required = false) String password,
                                     @RequestParam(value = "email",required = false) String email){

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

    @PatchMapping(path = "/{id}", headers = {"content-type=application/json"})
    public ResponseEntity updateUserFromJSON(@PathVariable String id, @RequestBody User user){

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

    @DeleteMapping("/{id}")
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

    @GetMapping("/registration/{token}")
    public ResponseEntity confirmRegistration(@PathVariable String token){
        try {
            userService.confirmRegistration(token);
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST,e.getMessage()));
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST,e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.build(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Response.build(HttpStatus.OK,"Confirmed"));
    }

    @GetMapping("/deletion/{token}")
    public ResponseEntity confirmDeletion(@PathVariable String token){
        try {
            userService.confirmDeletion(token);
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

        return ResponseEntity.status(HttpStatus.OK).body(Response.build(HttpStatus.OK, "Deleted"));
    }

    @GetMapping("/email-change/{token}")
    public ResponseEntity confirmEmailChange(@PathVariable String token){
        try {
            userService.confirmEmailChange(token);
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

        return ResponseEntity.status(HttpStatus.OK).body(Response.build(HttpStatus.OK, "Changed"));
    }

}
