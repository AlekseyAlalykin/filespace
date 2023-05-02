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

    @PostMapping(consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ResponseEntity postFilespaceConsumesURLEncoded(@RequestParam(value = "title", required = false) String title){
        Filespace filespace;

        try {
            if (title == null)
                throw new NullPointerException("No parameter \"title\" specified");

            filespace = filespaceService.createFilespace(securityUtil.getCurrentUser(), title);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST,e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(filespace);
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
            Long lId = Long.parseLong(id);
            filespace = filespaceService.getFilespaceById(securityUtil.getCurrentUser(), lId);

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

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filespace);
    }

    @PatchMapping(path = "/{id}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ResponseEntity updateFilespaceConsumesURLEncoded(
            @PathVariable String id,
            @RequestParam(required = false) String title){
        try {
            if (title == null)
                throw new NullPointerException("No parameter \"title\" specified");

            Long lId = Long.parseLong(id);
            filespaceService.updateFilespace(securityUtil.getCurrentUser(),lId,title);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK,"Filespace info changed"));
    }

    @PatchMapping(path = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateFilespace(@PathVariable String id, @RequestBody Filespace filespace){
        try {
            if (filespace.getTitle() == null)
                throw new NullPointerException("No parameter \"title\" specified");

            Long lId = Long.parseLong(id);
            filespaceService.updateFilespace(securityUtil.getCurrentUser(),lId, filespace.getTitle());
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Filespace info changed"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteFilespace(@PathVariable String id){
        try{
            Long lId = Long.parseLong(id);
            filespaceService.deleteFilespace(securityUtil.getCurrentUser(), lId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Filespace deleted"));
    }

    @PostMapping(path = "/{filespaceId}/files", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ResponseEntity attachFileToFilespaceConsumesURLEncoded(
            @PathVariable String filespaceId,
            @RequestParam(required = false) String fileId){
        FileFilespaceRelation relation;

        try {
            if (fileId == null)
                throw new NullPointerException("No parameter \"fileId\" specified");

            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lFileId = Long.parseLong(fileId);

            relation = filespaceService.attachFileToFilespace(securityUtil.getCurrentUser(),lFilespaceId,lFileId);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(relation);

    }

    @PostMapping(path = "/{filespaceId}/files", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity attachFileToFilespace(
            @PathVariable String filespaceId,
            @RequestBody File file){
        FileFilespaceRelation relation;

        try {
            if (file.getId() == null)
                throw new NullPointerException("No parameter \"fileId\" specified");

            Long lFilespaceId = Long.parseLong(filespaceId);

            relation = filespaceService.attachFileToFilespace(securityUtil.getCurrentUser(),
                    lFilespaceId,file.getId());
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(relation);

    }

    @GetMapping("/{id}/files")
    public ResponseEntity getFilesFromFilespace(
            @PathVariable String id,
            @RequestParam(name = "q", required = false) String query){
        List<FilespaceFileInfo> list;

        try {
            Long lId = Long.parseLong(id);

            if (query == null)
                query = "";

            list = filespaceService.getFilesFromFilespace(securityUtil.getCurrentUser(), lId, query);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(list);
    }

    @GetMapping("/{id}/users")
    public ResponseEntity getUsersFromFilespace(
            @PathVariable String id,
            @RequestParam(name = "q", required = false) String query){
        List<FilespaceUserInfo> list;

        try {
            Long lId = Long.parseLong(id);

            if (query == null)
                query = "";

            list = filespaceService.getUsersOfFilespace(securityUtil.getCurrentUser(), lId, query);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(list);
    }

    @PostMapping(path = "/{id}/users", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ResponseEntity addUserToFilespaceConsumesURLEncoded(
            @PathVariable String id,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean allowDownload,
            @RequestParam(required = false) Boolean allowUpload,
            @RequestParam(required = false) Boolean allowDeletion,
            @RequestParam(required = false) Boolean allowUserManagement,
            @RequestParam(required = false) Boolean allowFilespaceManagement){

        UserFilespaceRelation relation = new UserFilespaceRelation();

        try {
            if (userId == null && username == null)
                throw new NullPointerException("No user specified through \"userId\" or \"username\"");

            Long lFilespaceId = Long.parseLong(id);

            User user = new User();
            if (userId != null)
                user.setId(Long.parseLong(userId));
            user.setUsername(username);

            relation.setUser(user);
            relation.setAllowDownload(Boolean.TRUE.equals(allowDownload));
            relation.setAllowUpload(Boolean.TRUE.equals(allowUpload));
            relation.setAllowDeletion(Boolean.TRUE.equals(allowDeletion));
            relation.setAllowUserManagement(Boolean.TRUE.equals(allowUserManagement));
            relation.setAllowFilespaceManagement(Boolean.TRUE.equals(allowFilespaceManagement));

            relation = filespaceService.attachUserToFilespace(securityUtil.getCurrentUser(), lFilespaceId, relation);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(relation);
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


            Long lFilespaceId = Long.parseLong(id);

            newRelation = filespaceService.attachUserToFilespace(securityUtil.getCurrentUser(), lFilespaceId, relation);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(newRelation);
    }

    @DeleteMapping(path = "/{filespaceId}/users/{userId}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ResponseEntity deleteUserFromFilespaceConsumesURLEncoded(
            @PathVariable String filespaceId,
            @PathVariable String userId,
            @RequestParam(required = false) String deleteFiles){
        try {
            Long lFilespaceId = Long.parseLong(filespaceId);

            Long lUserId = securityUtil.getUserId(userId);

            Boolean bDeleteFiles = Boolean.valueOf(deleteFiles);

            filespaceService.detachUserFromFilespace(securityUtil.getCurrentUser(), lFilespaceId, lUserId, bDeleteFiles);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Removed"));
    }

    @DeleteMapping(path = "/{filespaceId}/users/{userId}")
    public ResponseEntity deleteUserFromFilespace(
            @PathVariable String filespaceId,
            @PathVariable String userId,
            @RequestBody(required = false) String jsonBody){
        try {
            Long lFilespaceId = Long.parseLong(filespaceId);

            Long lUserId = securityUtil.getUserId(userId);

            Boolean bDeleteFiles;

            if (jsonBody != null) {
                Map<String, Object> result =
                        new ObjectMapper().readValue(jsonBody, HashMap.class);

                bDeleteFiles = Boolean.TRUE.equals(result.get("deleteFiles"));
            } else
                bDeleteFiles = false;

            filespaceService.detachUserFromFilespace(securityUtil.getCurrentUser(), lFilespaceId,lUserId, bDeleteFiles);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Removed"));
    }


    @DeleteMapping("/{filespaceId}/files/{fileId}")
    public ResponseEntity deleteFileFromFilespace(
            @PathVariable String filespaceId,
            @PathVariable String fileId){

        try {
            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lFileId = Long.parseLong(fileId);

            filespaceService.detachFileFromFilespace(securityUtil.getCurrentUser(),lFilespaceId,lFileId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Removed"));
    }

    @PatchMapping(path = "/{filespaceId}/users/{userId}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE } )
    public ResponseEntity updateUserPermissionsConsumesURLEncoded(
            @PathVariable String filespaceId,
            @PathVariable String userId,
            @RequestParam(required = false) Boolean allowDownload,
            @RequestParam(required = false) Boolean allowUpload,
            @RequestParam(required = false) Boolean allowDeletion,
            @RequestParam(required = false) Boolean allowUserManagement,
            @RequestParam(required = false) Boolean allowFilespaceManagement){
        UserFilespaceRelation relation = new UserFilespaceRelation();
        try {
            Long lUserId = securityUtil.getUserId(userId);

            User user = new User();
            user.setId(lUserId);

            Filespace filespace = new Filespace();
            filespace.setId(Long.parseLong(filespaceId));

            relation.setFilespace(filespace);
            relation.setUser(user);
            relation.setAllowDownload(Boolean.TRUE.equals(allowDownload));
            relation.setAllowUpload(Boolean.TRUE.equals(allowUpload));
            relation.setAllowDeletion(Boolean.TRUE.equals(allowDeletion));
            relation.setAllowUserManagement(Boolean.TRUE.equals(allowUserManagement));
            relation.setAllowFilespaceManagement(Boolean.TRUE.equals(allowFilespaceManagement));

            filespaceService.updateUserPermissions(securityUtil.getCurrentUser(), relation);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Permissions updated"));
    }

    @PatchMapping(path = "/{filespaceId}/users/{userId}", consumes = { MediaType.APPLICATION_JSON_VALUE } )
    public ResponseEntity updateUserPermissions(
            @PathVariable String filespaceId,
            @PathVariable String userId,
            @RequestBody UserFilespaceRelation relation){
        try {
            Long lUserId = securityUtil.getUserId(userId);

            User user = new User();
            user.setId(lUserId);
            Filespace filespace = new Filespace();
            filespace.setId(Long.parseLong(filespaceId));

            relation.setUser(user);
            relation.setFilespace(filespace);

            filespaceService.updateUserPermissions(securityUtil.getCurrentUser(), relation);
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "Role changed"));
    }
}
