package org.filespace.controllers;

import org.filespace.model.Filespace;
import org.filespace.security.SecurityUtil;
import org.filespace.services.IntegratedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/filespaces")
public class APIFilespaceController {

    @Autowired
    private IntegratedService integratedService;

    @GetMapping
    public String getFilespace(){

        return "temp";
    }

    @PostMapping
    public ResponseEntity postFilespace(@RequestParam String title){

        String username = SecurityUtil.getCurrentUserUsername();

        try {
            integratedService.createFilespace(username,title);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("New filespace successfully created");
    }

    @GetMapping("/{id}")
    public ResponseEntity getFilespace(@PathVariable String id){
        Filespace filespace;

        try {
            Long lId = Long.parseLong(id);
            filespace = integratedService.getFilespaceById(SecurityUtil.getCurrentUserUsername(), lId);

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

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filespace);
    }

    @PatchMapping("/{id}")
    public ResponseEntity patchFilespace(@PathVariable String id, @RequestParam String title){

        //return ResponseEntity.status(HttpStatus.OK).body();
        return null;
    }

    @DeleteMapping("/{id}")
    public String deleteFilespace(@PathVariable String id){

        return "temp";
    }

    @PostMapping("/{filespaceId}/files")
    public String postFileIntoFilespace(@PathVariable String filespaceId){

        return "temp";
    }

    @PostMapping("/{filespaceId}/users")
    public String postUserIntoFilespace(@PathVariable String filespaceId){

        return "temp";
    }

    @DeleteMapping("/{filespaceId}/users/{userId}")
    public String deleteUserFromFilespace(@PathVariable String filespaceId, @PathVariable String userId){

        return "temp";
    }

    @DeleteMapping("/{filespaceId}/files/{fileId}")
    public String deleteFileFromFilespace(@PathVariable String filespaceId, @PathVariable String fileId){

        return "temp";
    }

}
