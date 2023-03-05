package org.filespace.controllers;

import org.filespace.config.Response;
import org.filespace.model.compoundrelations.FileFilespaceRelation;
import org.filespace.model.entities.Role;
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

@CrossOrigin
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
        Filespace filespace;

        try {
            Long lId = Long.parseLong(id);
            filespace = filespaceService.getFilespaceById(securityUtil.getCurrentUser(), lId);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND,e.getMessage()));

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN,e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST,e.getMessage()));
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
            filespaceService.updateFilespaceTitle(securityUtil.getCurrentUser(),lId,title);
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
            filespaceService.updateFilespaceTitle(securityUtil.getCurrentUser(),lId, filespace.getTitle());
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
    public ResponseEntity attachFileToFilespaceFromJSON(@PathVariable String filespaceId, @RequestBody FileFilespaceRelation relation){

        try {
            if (relation.getKey().getFileId() == null)
                throw new NullPointerException("No parameter \"fileId\" specified");

            Long lFilespaceId = Long.parseLong(filespaceId);

            relation = filespaceService.attachFileToFilespace(securityUtil.getCurrentUser(),
                    lFilespaceId,relation.getKey().getFileId());
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
    public ResponseEntity getFilesFromFilespace(@PathVariable String id){
        List<FilespaceFileInfo> list;

        try {
            Long lId = Long.parseLong(id);

            list = filespaceService.getFilesFromFilespace(securityUtil.getCurrentUser(), lId);
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
    public ResponseEntity getUsersOfFilespace(@PathVariable String id){
        List<FilespaceUserInfo> list;

        try {
            Long lId = Long.parseLong(id);

            list = filespaceService.getUsersOfFilespace(securityUtil.getCurrentUser(), lId);
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

            relation = filespaceService.attachUserToFilespace(securityUtil.getCurrentUser(), lFilespaceId, lUserId, userRole);
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
    public ResponseEntity addUsersToFilespaceFromJSON(@PathVariable String id, @RequestBody UserFilespaceRelation relation){
        UserFilespaceRelation newRelation;
        System.out.println(relation.getKey().getUserId());

        try {
            if (relation.getKey().getUserId() == null)
                throw new NullPointerException("No parameter \"userId\" specified");

            if (relation.getRole() == null)
                throw new NullPointerException("No parameter \"role\" specified");

            Long lFilespaceId = Long.parseLong(id);

            newRelation = filespaceService.attachUserToFilespace(securityUtil.getCurrentUser(), lFilespaceId, relation.getKey().getUserId(), relation.getRole());
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
                .body(newRelation);
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
    public ResponseEntity updateUserRole(@PathVariable(value = "filespaceId") String filespaceId,
                                         @PathVariable(value = "userId") String userId,
                                         @RequestParam(value = "role") String role){
        try {
            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lUserId = Long.parseLong(userId);
            Role newRole = Role.valueOf(role.toUpperCase());

            filespaceService.updateUserRole(securityUtil.getCurrentUser(),lFilespaceId,lUserId, newRole);
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

    @PatchMapping(path = "/{filespaceId}/users/{userId}", headers = {"content-type=application/json"} )
    public ResponseEntity updateUserRoleFromJSON(@PathVariable(value = "filespaceId") String filespaceId,
                                         @PathVariable(value = "userId") String userId,
                                         @RequestBody UserFilespaceRelation relation){
        try {
            if (relation.getRole() == null)
                throw new Exception("No role specified");

            Long lFilespaceId = Long.parseLong(filespaceId);
            Long lUserId = Long.parseLong(userId);

            filespaceService.updateUserRole(securityUtil.getCurrentUser(),lFilespaceId,lUserId, relation.getRole());
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
