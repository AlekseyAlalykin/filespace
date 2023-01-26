package org.filespace.controllers;

import org.filespace.model.compoundrelations.FileFilespaceRelation;
import org.filespace.model.compoundrelations.Role;
import org.filespace.model.compoundrelations.UserFilespaceRelation;
import org.filespace.model.intermediate.FilespaceFileInfo;
import org.filespace.model.intermediate.FilespaceRole;
import org.filespace.model.entities.Filespace;
import org.filespace.model.intermediate.FilespaceUserInfo;
import org.filespace.security.SecurityUtil;
import org.filespace.services.FilespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/filespaces")
public class FilespaceControllerAPI {

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private FilespaceService filespaceService;

    @GetMapping
    public ResponseEntity getFilespaces(){

        List<FilespaceRole> filespaces = null;

        try {
            filespaces = filespaceService.getUserFilespaces(securityUtil.getCurrentUser());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong, try again later");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(filespaces);

    }

    @PostMapping
    public ResponseEntity postFilespace(@RequestParam(value = "title", required = false) String title){
        Filespace filespace;

        try {
            if (title == null)
                throw new NullPointerException("No parameter \"title\" specified");

            filespace = filespaceService.createFilespace(securityUtil.getCurrentUser(), title);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(filespace);
    }

    @GetMapping("/{id}")
    public ResponseEntity getFilespace(@PathVariable String id){
        Filespace filespace;

        try {
            Long lId = Long.parseLong(id);
            filespace = filespaceService.getFilespaceById(securityUtil.getCurrentUser(), lId);

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
    public ResponseEntity updateFilespace(@PathVariable String id,
                                         @RequestParam(value = "title",required = false) String title){
        try {
            if (title == null)
                throw new NullPointerException("No parameter \"title\" specified");

            Long lId = Long.parseLong(id);
            filespaceService.updateFilespaceTitle(securityUtil.getCurrentUser(),lId,title);
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
            filespaceService.deleteFilespace(securityUtil.getCurrentUser(), lId);
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
    public ResponseEntity attachFileToFilespace(@PathVariable String filespaceId,
                                                @RequestParam(value = "fileId", required = false) String fileId){
        FileFilespaceRelation relation;

        try {
            if (fileId == null)
                throw new NullPointerException("No parameter \"fileId\" specified");

            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lFileId = Long.parseLong(fileId);

            relation = filespaceService.attachFileToFilespace(securityUtil.getCurrentUser(),lFilespaceId,lFileId);
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
                .body(relation);

    }

    @GetMapping("/{id}/files")
    public ResponseEntity getFilesFromFilespace(@PathVariable String id){
        List<FilespaceFileInfo> list;

        try {
            Long lId = Long.parseLong(id);

            list = filespaceService.getFilesFromFilespace(securityUtil.getCurrentUser(), lId);
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

            list = filespaceService.getUsersOfFilespace(securityUtil.getCurrentUser(), lId);
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
    public ResponseEntity addUsersToFilespace(@PathVariable String id,
                                              @RequestParam(value = "userId", required = false) String userId,
                                              @RequestParam(value = "role", required = false) String role){
        UserFilespaceRelation relation;

        try {
            if (userId == null)
                throw new NullPointerException("No parameter \"userId\" specified");

            if (role == null)
                throw new NullPointerException("No parameter \"role\" specified");

            Long lFilespaceId = Long.parseLong(id);
            Long lUserId = Long.parseLong(userId);
            Role userRole = Role.valueOf(role.toUpperCase());

            relation = filespaceService.addUserToFilespace(securityUtil.getCurrentUser(), lFilespaceId, lUserId, userRole);
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
                .body(relation);
    }

    @DeleteMapping("/{filespaceId}/users/{userId}")
    public ResponseEntity deleteUserFromFilespace(@PathVariable String filespaceId,
                                                  @PathVariable(value = "userId") String userId,
                                                  @RequestParam(value = "deleteFiles",required = false) String deleteFiles){
        try {
            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lUserId = Long.parseLong(userId);
            Boolean bDeleteFiles = Boolean.valueOf(deleteFiles);

            System.out.println(bDeleteFiles);

            filespaceService.deleteUserFromFilespace(securityUtil.getCurrentUser(), lFilespaceId,lUserId, bDeleteFiles);
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
    public ResponseEntity deleteFileFromFilespace(@PathVariable(value = "filespaceId") String filespaceId,
                                                  @PathVariable(value = "fileId") String fileId){

        try {
            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lFileId = Long.parseLong(fileId);

            filespaceService.deleteFileFromFilespace(securityUtil.getCurrentUser(),lFilespaceId,lFileId);
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
    public ResponseEntity updateUserRole(@PathVariable(value = "filespaceId") String filespaceId,
                                         @PathVariable(value = "userId") String userId,
                                         @RequestParam(value = "role") String role){
        try {
            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lUserId = Long.parseLong(userId);
            Role newRole = Role.valueOf(role);

            filespaceService.updateUserRole(securityUtil.getCurrentUser(),lFilespaceId,lUserId, newRole);
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
}
