package org.filespace.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.filespace.config.Response;
import org.filespace.model.entities.compoundrelations.FileFilespaceRelation;
import org.filespace.model.entities.simplerelations.File;
import org.filespace.model.entities.compoundrelations.UserFilespaceRelation;
import org.filespace.model.entities.simplerelations.User;
import org.filespace.model.intermediate.FilespaceFileInfo;
import org.filespace.model.intermediate.FilespacePermissions;
import org.filespace.model.entities.simplerelations.Filespace;
import org.filespace.model.intermediate.FilespaceUserInfo;
import org.filespace.security.SecurityUtil;
import org.filespace.services.FilespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/filespaces")
public class FilespaceControllerAPI {

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private FilespaceService filespaceService;

    @GetMapping
    public ResponseEntity getFilespaces(@RequestParam(name = "q", required = false) String title){

        List<FilespacePermissions> filespaces = null;

        try {
            if (title == null)
                title = "";

            filespaces = filespaceService.getUserFilespacesByTitle(securityUtil.getCurrentUser(), title);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.build(HttpStatus.INTERNAL_SERVER_ERROR,"Something went wrong, try again later"));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(filespaces);

    }

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity postFilespace(@RequestBody Filespace filespace){

        try {
            if (filespace.getTitle() == null)
                throw new NullPointerException("No parameter \"title\" specified");

            filespace = filespaceService.createFilespace(securityUtil.getCurrentUser(), filespace.getTitle());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST,e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(filespace);
    }

    @GetMapping("/{id}")
    public ResponseEntity getFilespace(@PathVariable String id){
        FilespacePermissions filespace;

        try {
            Integer filespaceId = Integer.parseInt(id);
            filespace = filespaceService.getFilespaceById(securityUtil.getCurrentUser(), filespaceId);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filespace);
    }

    @PatchMapping(path = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateFilespace(@PathVariable String id, @RequestBody Filespace filespace){
        try {
            if (filespace.getTitle() == null)
                throw new NullPointerException("No parameter \"title\" specified");

            Integer filespaceId = Integer.parseInt(id);
            filespaceService.updateFilespace(securityUtil.getCurrentUser(),filespaceId, filespace.getTitle());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Filespace info changed"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteFilespace(@PathVariable String id){
        try{
            Integer filespaceId = Integer.parseInt(id);
            filespaceService.deleteFilespace(securityUtil.getCurrentUser(), filespaceId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (NumberFormatException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Filespace deleted"));
    }

    @PostMapping(path = "/{filespaceId}/files", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity attachFileToFilespace(
            @PathVariable String filespaceId,
            @RequestBody File file){
        FileFilespaceRelation relation;

        try {
            if (file.getId() == null)
                throw new NullPointerException("No parameter \"fileId\" specified");

            Integer iFilespaceId = Integer.parseInt(filespaceId);

            relation = filespaceService.attachFileToFilespace(securityUtil.getCurrentUser(),
                    iFilespaceId,file.getId());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Response.build(HttpStatus.CONFLICT, e.getMessage()));
        } catch (NumberFormatException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(relation);

    }

    @GetMapping("/{id}/files")
    public ResponseEntity getFilesFromFilespace(
            @PathVariable String id,
            @RequestParam(name = "q", required = false) String query){
        List<FilespaceFileInfo> list;

        try {
            Integer filespaceId = Integer.parseInt(id);

            if (query == null)
                query = "";

            list = filespaceService.getFilesFromFilespace(securityUtil.getCurrentUser(), filespaceId, query);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(list);
    }

    @GetMapping("/{id}/users")
    public ResponseEntity getUsersFromFilespace(
            @PathVariable String id,
            @RequestParam(name = "q", required = false) String query){
        List<FilespaceUserInfo> list;

        try {
            Integer filespaceId = Integer.parseInt(id);

            if (query == null)
                query = "";

            list = filespaceService.getUsersOfFilespace(securityUtil.getCurrentUser(), filespaceId, query);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(list);
    }

    @PostMapping(path = "/{id}/users", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addUserToFilespace(@PathVariable String id, @RequestBody UserFilespaceRelation relation){
        UserFilespaceRelation newRelation;

        try {
            if (relation == null)
                throw new NullPointerException("Couldn't convert to relation");

            if (relation.getUser().getId() == null && relation.getUser().getUsername() == null)
                throw new NullPointerException("No user specified through \"userId\" or \"username\"");

            relation.setAllowFilespaceManagement(Boolean.TRUE.equals(relation.allowFilespaceManagement()));
            relation.setAllowUserManagement(Boolean.TRUE.equals(relation.allowUserManagement()));
            relation.setAllowDownload(Boolean.TRUE.equals(relation.allowDownload()));
            relation.setAllowUpload(Boolean.TRUE.equals(relation.allowUpload()));
            relation.setAllowDeletion(Boolean.TRUE.equals(relation.allowDeletion()));


            Integer iFilespaceId = Integer.parseInt(id);

            newRelation = filespaceService.attachUserToFilespace(securityUtil.getCurrentUser(), iFilespaceId, relation);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Response.build(HttpStatus.CONFLICT, e.getMessage()));

        } catch (NullPointerException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newRelation);
    }



    @DeleteMapping(path = "/{filespaceId}/users/{userId}")
    public ResponseEntity deleteUserFromFilespace(
            @PathVariable String filespaceId,
            @PathVariable String userId,
            @RequestBody(required = false) String jsonBody){
        try {
            Integer iFilespaceId = Integer.parseInt(filespaceId);

            Integer iUserId = securityUtil.getUserId(userId);

            Boolean bDeleteFiles;

            if (jsonBody != null) {
                Map<String, Object> result = new ObjectMapper().readValue(jsonBody, HashMap.class);

                bDeleteFiles = Boolean.TRUE.equals(result.get("deleteFiles"));
            } else
                bDeleteFiles = false;

            filespaceService.detachUserFromFilespace(securityUtil.getCurrentUser(), iFilespaceId,iUserId, bDeleteFiles);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Response.build(HttpStatus.CONFLICT, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Removed"));
    }


    @DeleteMapping("/{filespaceId}/files/{fileId}")
    public ResponseEntity deleteFileFromFilespace(
            @PathVariable String filespaceId,
            @PathVariable String fileId){

        try {
            Integer iFilespaceId = Integer.parseInt(filespaceId);
            Integer iFileId = Integer.parseInt(fileId);

            filespaceService.detachFileFromFilespace(securityUtil.getCurrentUser(),iFilespaceId,iFileId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (NumberFormatException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Response.build(HttpStatus.CONFLICT, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Removed"));
    }


    @PatchMapping(path = "/{filespaceId}/users/{userId}", consumes = { MediaType.APPLICATION_JSON_VALUE } )
    public ResponseEntity updateUserPermissions(
            @PathVariable String filespaceId,
            @PathVariable String userId,
            @RequestBody UserFilespaceRelation relation){
        try {
            Integer iUserId = securityUtil.getUserId(userId);

            User user = new User();
            user.setId(iUserId);
            Filespace filespace = new Filespace();
            filespace.setId(Integer.parseInt(filespaceId));

            relation.setUser(user);
            relation.setFilespace(filespace);

            filespaceService.updateUserPermissions(securityUtil.getCurrentUser(), relation);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Role changed"));
    }
}
