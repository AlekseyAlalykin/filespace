package org.filespace.controllers;

import org.filespace.config.Response;
import org.filespace.model.entities.simplerelations.User;
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
@RequestMapping("/api/users")
public class UserControllerAPI {

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionManager sessionManager;

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity createUser(@RequestBody User user){

        try {
            if (user.getUsername() == null)
                throw new IllegalArgumentException("No username specified");
            if (user.getEmail() == null)
                throw new IllegalArgumentException("No email specified");
            if (user.getPassword() == null)
                throw new IllegalArgumentException("No password specified");

            user = userService.registerUser(user.getUsername(), user.getPassword(), user.getEmail());

        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Response.build(HttpStatus.CONFLICT, e.getMessage()));
        }


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(user);
    }

    @GetMapping(path = "/current/logout")
    public ResponseEntity logout(){
        sessionManager.closeAllUserSessions(securityUtil.getCurrentUser());
        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, HttpStatus.OK.getReasonPhrase()));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity getUser(@PathVariable String id){
        Object user;

        try {
            Integer userId = securityUtil.getUserId(id);

            user = userService.getUserById(userId);
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, "No user with such id found"));
        } catch (NumberFormatException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, "No user with such id found"));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(user);
    }

    @GetMapping
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

    @PatchMapping(path = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateUser(@PathVariable String id, @RequestBody User user){

        try {
            Integer userId = securityUtil.getUserId(id);

            userService.updateUser(securityUtil.getCurrentUser(), userId, user.getUsername(), user.getPassword(), user.getEmail());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Response.build(HttpStatus.CONFLICT, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Response.build(HttpStatus.OK,"OK"));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity deleteUser(@PathVariable String id){

        try {
            Integer userId = securityUtil.getUserId(id);

            userService.deleteUser(securityUtil.getCurrentUser(), userId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        }

        return ResponseEntity.status(HttpStatus.OK).body(Response.build(HttpStatus.OK,"OK"));
    }

    @GetMapping(path = "/token/{token}")
    public ResponseEntity confirmToken(@PathVariable String token){
        String message;
        try {
            message = userService.confirmToken(token);
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Response.build(HttpStatus.CONFLICT, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(Response.build(HttpStatus.OK, message));
    }

}
