package org.filespace.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.filespace.config.Response;
import org.filespace.model.compoundrelations.FileFilespaceRelation;
import org.filespace.model.entities.File;
import org.filespace.model.compoundrelations.UserFilespaceRelation;
import org.filespace.model.entities.User;
import org.filespace.model.intermediate.FilespaceFileInfo;
import org.filespace.model.intermediate.FilespacePermissions;
import org.filespace.model.entities.Filespace;
import org.filespace.model.intermediate.FilespaceUserInfo;
import org.filespace.security.SecurityUtil;
import org.filespace.services.FilespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
                filespaces = filespaceService.getUserFilespaces(securityUtil.getCurrentUser());
            else
                filespaces = filespaceService.getUserFilespacesByTitle(securityUtil.getCurrentUser(), title);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.build(HttpStatus.INTERNAL_SERVER_ERROR,"Something went wrong, try again later"));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(filespaces);

    }

    @PostMapping(headers = {"content-type=application/x-www-form-urlencoded"})
    public ResponseEntity postFilespace(@RequestParam(value = "title", required = false) String title){
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

    @PostMapping(headers = {"content-type=application/json"})
    public ResponseEntity postFilespaceFromJSON(@RequestBody Filespace filespace){

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

    @PatchMapping(path = "/{id}", headers = {"content-type=application/x-www-form-urlencoded"})
    public ResponseEntity updateFilespace(@PathVariable String id,
                                         @RequestParam(value = "title",required = false) String title){
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

    @PatchMapping(path = "/{id}", headers = {"content-type=application/json"})
    public ResponseEntity updateFilespaceFromJSON(@PathVariable String id, @RequestBody Filespace filespace){
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

    @PostMapping(path = "/{filespaceId}/files", headers = {"content-type=application/x-www-form-urlencoded"})
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

    @PostMapping(path = "/{filespaceId}/files", headers = {"content-type=application/json"})
    public ResponseEntity attachFileToFilespaceFromJSON(@PathVariable String filespaceId,
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
    public ResponseEntity getFilesFromFilespace(@PathVariable String id,
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
    public ResponseEntity getUsersOfFilespace(@PathVariable String id,
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

    @PostMapping(path = "/{id}/users", headers = {"content-type=application/x-www-form-urlencoded"})
    public ResponseEntity addUserToFilespace(@PathVariable String id,
                                             @RequestParam(value = "userId", required = false) String userId,
                                             @RequestParam(value = "username", required = false) String username,
                                             @RequestParam(value = "allowDownload", required = false) Boolean allowDownload,
                                             @RequestParam(value = "allowUpload", required = false) Boolean allowUpload,
                                             @RequestParam(value = "allowDeletion", required = false) Boolean allowDeletion,
                                             @RequestParam(value = "allowUserManagement", required = false) Boolean allowUserManagement,
                                             @RequestParam(value = "allowFilespaceManagement", required = false) Boolean allowFilespaceManagement){

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

    @PostMapping(path = "/{id}/users", headers = {"content-type=application/json"})
    public ResponseEntity addUserToFilespaceFromJSON(@PathVariable String id, @RequestBody UserFilespaceRelation relation){
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

    @DeleteMapping(path = "/{filespaceId}/users/{userId}", headers = {"content-type=application/x-www-form-urlencoded"})
    public ResponseEntity deleteUserFromFilespace(@PathVariable String filespaceId,
                                                  @PathVariable(value = "userId") String userId,
                                                  @RequestParam(value = "deleteFiles",required = false) String deleteFiles){
        try {
            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lUserId = Long.parseLong(userId);
            Boolean bDeleteFiles = Boolean.valueOf(deleteFiles);

            System.out.println(bDeleteFiles);

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

    @DeleteMapping(path = "/{filespaceId}/users/{userId}")
    public ResponseEntity deleteUserFromFilespaceJSON(@PathVariable String filespaceId,
                                                  @PathVariable(value = "userId") String userId,
                                                  @RequestBody(required = false) String jsonBody){
        try {
            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lUserId = Long.parseLong(userId);

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
    public ResponseEntity deleteFileFromFilespace(@PathVariable(value = "filespaceId") String filespaceId,
                                                  @PathVariable(value = "fileId") String fileId){

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

    @PatchMapping(path = "/{filespaceId}/users/{userId}", headers = {"content-type=application/x-www-form-urlencoded"} )
    public ResponseEntity updateUserPermissions(@PathVariable(value = "filespaceId") String filespaceId,
                                                @PathVariable(value = "userId") String userId,
                                                @RequestParam(value = "allowDownload", required = false) Boolean allowDownload,
                                                @RequestParam(value = "allowUpload", required = false) Boolean allowUpload,
                                                @RequestParam(value = "allowDeletion", required = false) Boolean allowDeletion,
                                                @RequestParam(value = "allowUserManagement", required = false) Boolean allowUserManagement,
                                                @RequestParam(value = "allowFilespaceManagement", required = false) Boolean allowFilespaceManagement){
        UserFilespaceRelation relation = new UserFilespaceRelation();
        try {
            User user = new User();
            user.setId(Long.parseLong(userId));

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

    @PatchMapping(path = "/{filespaceId}/users/{userId}", headers = {"content-type=application/json"} )
    public ResponseEntity updateUserRoleFromJSON(@PathVariable(value = "filespaceId") String filespaceId,
                                                @PathVariable(value = "userId") String userId,
                                                @RequestBody UserFilespaceRelation relation){
        try {

            User user = new User();
            user.setId(Long.parseLong(userId));
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
