package org.filespace.controllers;

import org.filespace.model.compoundrelations.Role;
import org.filespace.model.intermediate.FilespaceFileInfo;
import org.filespace.model.intermediate.FilespaceRole;
import org.filespace.model.entities.Filespace;
import org.filespace.model.intermediate.FilespaceUserInfo;
import org.filespace.security.SecurityUtil;
import org.filespace.services.IntegratedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/filespaces")
public class FilespaceControllerAPI {

    @Autowired
    private IntegratedService integratedService;

    @GetMapping
    public ResponseEntity getFilespaces(){

        List<FilespaceRole> filespaces = null;

        try {
            filespaces = integratedService.getUserFilespaces(SecurityUtil.getCurrentUserUsername());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong, try again later");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(filespaces);

    }

    @PostMapping
    public ResponseEntity postFilespace(@RequestParam(required = false) String title){
        try {
            if (title == null)
                throw new NullPointerException("No parameter \"title\" specified");

            integratedService.createFilespace(SecurityUtil.getCurrentUserUsername(),title);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("New filespace created");
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
    public ResponseEntity patchFilespace(@PathVariable String id,
                                         @RequestParam(required = false) String title){
        try {
            if (title == null)
                throw new NullPointerException("No parameter \"title\" specified");

            Long lId = Long.parseLong(id);
            integratedService.updateFilespaceTitle(SecurityUtil.getCurrentUserUsername(),lId,title);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body("Filespace info changed");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteFilespace(@PathVariable String id){
        try{
            Long lId = Long.parseLong(id);
            integratedService.deleteFilespace(SecurityUtil.getCurrentUserUsername(), lId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body("Filespace deleted");
    }

    @PostMapping("/{filespaceId}/files")
    public ResponseEntity postFileToFilespace(@PathVariable String filespaceId,
                                              @RequestParam(required = false) String fileId){

        try {
            if (fileId == null)
                throw new NullPointerException("No parameter \"fileId\" specified");

            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lFileId = Long.parseLong(fileId);

            integratedService.addFileToFilespace(SecurityUtil.getCurrentUserUsername(),lFilespaceId,lFileId);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body("File added to filespace");

    }

    @GetMapping("/{id}/files")
    public ResponseEntity getFilesFromFilespace(@PathVariable String id){
        List<FilespaceFileInfo> list;

        try {
            Long lId = Long.parseLong(id);

            list = integratedService.getFilesFromFilespace(SecurityUtil.getCurrentUserUsername(), lId);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(list);
    }

    @GetMapping("/{id}/users")
    public ResponseEntity getUsersOfFilespace(@PathVariable String id){
        List<FilespaceUserInfo> list;

        try {
            Long lId = Long.parseLong(id);

            list = integratedService.getUsersOfFilespace(SecurityUtil.getCurrentUserUsername(), lId);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(list);
    }

    @PostMapping("/{id}/users")
    public ResponseEntity postUsersToFilespace(@PathVariable String id,
                                               @RequestParam(required = false) String userId,
                                               @RequestParam(required = false) String role){

        try {
            if (userId == null)
                throw new NullPointerException("No parameter \"userId\" specified");

            if (role == null)
                throw new NullPointerException("No parameter \"role\" specified");

            Long lFilespaceId = Long.parseLong(id);
            Long lUserId = Long.parseLong(userId);
            Role userRole = Role.valueOf(role.toUpperCase());

            integratedService.addUserToFilespace(SecurityUtil.getCurrentUserUsername(), lFilespaceId, lUserId, userRole);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body("User added to filespace");
    }

    @DeleteMapping("/{filespaceId}/users/{userId}")
    public ResponseEntity deleteUserFromFilespace(@PathVariable String filespaceId,
                                                  @PathVariable String userId,
                                                  @RequestParam(required = false) String deleteFiles){
        try {
            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lUserId = Long.parseLong(userId);
            Boolean bDeleteFiles = Boolean.valueOf(deleteFiles);

            System.out.println(bDeleteFiles);

            integratedService.deleteUserFromFilespace(SecurityUtil.getCurrentUserUsername(),
                    lFilespaceId,lUserId, bDeleteFiles);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body("Removed");
    }

    @DeleteMapping("/{filespaceId}/files/{fileId}")
    public ResponseEntity deleteFileFromFilespace(@PathVariable String filespaceId,
                                                  @PathVariable String fileId){

        try {
            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lFileId = Long.parseLong(fileId);
            String requestSender = SecurityUtil.getCurrentUserUsername();

            integratedService.deleteFileFromFilespace(requestSender,lFilespaceId,lFileId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body("Removed");
    }

    @PatchMapping("/{filespaceId}/users/{userId}")
    public ResponseEntity patchUserRole(@PathVariable String filespaceId,
                                        @PathVariable String userId,
                                        @RequestParam String role){
        try {
            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lUserId = Long.parseLong(userId);
            Role newRole = Role.valueOf(role);

            String requestSender = SecurityUtil.getCurrentUserUsername();
            integratedService.patchUserRole(requestSender,lFilespaceId,lUserId, newRole);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body("Role changed");
    }











    //Думаю не надо либо доделать поиск filespace и в нем юзера или файла
    /*
    @GetMapping("/{filespaceId}/files/{fileId}")
    public ResponseEntity getFileFromFilespace(@PathVariable String filespaceId, @PathVariable String fileId){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location",  "/api/files/" + fileId);

        return new ResponseEntity<String>(headers,HttpStatus.FOUND);
    }

    @GetMapping("/{filespaceId}/files/{userId}")
    public ResponseEntity getUserFromFilespace(@PathVariable String filespaceId, @PathVariable String userId){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location",  "/api/users/" + userId);

        return new ResponseEntity<String>(headers,HttpStatus.FOUND);
    }

     */
}
