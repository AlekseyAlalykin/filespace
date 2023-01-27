package org.filespace.controllers;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.filespace.model.entities.File;
import org.filespace.security.SecurityUtil;
import org.filespace.services.DiskStorageService;
import org.filespace.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileControllerAPI {

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private FileService fileService;

    @GetMapping
    public ResponseEntity getFiles(){
        List<File> files = null;

        try {
            files = fileService.getUserFiles(securityUtil.getCurrentUser());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong, try again later");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(files);
    }

    @PostMapping
    public ResponseEntity postFile(HttpServletRequest request){

        long length = request.getContentLengthLong();
        if (length == -1 || length > DiskStorageService.getMaxContentLength())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Content length is unknown or exceeds set limit of: "
                            + DiskStorageService.getMaxContentLength());

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        List<File> files;

        //Если мултипарт то загрузка нового файла иначе копирование
        if (isMultipart){
            try {
                files = fileService.saveFileFromUser(request, securityUtil.getCurrentUser());

            } catch (FileUploadException e) {
                e.printStackTrace();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("File upload error: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Internal server error: " + e.getMessage());
            } catch (IllegalStateException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("File upload error: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Internal server error: " + e.getMessage());
            }

        } else {
            String fileIdParam = request.getParameter("fileId");

            if (fileIdParam == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No fileId parameter");

            try {
                Long fileId = Long.parseLong(fileIdParam);

                File file = fileService.copyFile(securityUtil.getCurrentUser(),fileId);

                files = new LinkedList<>();
                files.add(file);
            }  catch (IllegalAccessException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Internal server error: " + e.getMessage());
            }
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(files);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getFile(@PathVariable String id){
        List<Object> list = null;

        try {
            Long lId = Long.parseLong(id);

            list = fileService.sendFileToUser(securityUtil.getCurrentUser(), lId);
        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        File file = (File) list.get(0);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, file.getSize().toString())
                .body((InputStreamResource) list.get(1));
    }

    @PatchMapping("/{id}")
    public ResponseEntity updateFileInfo(@PathVariable String id,
                                         @RequestParam(value = "comment", required = false) String comment,
                                         @RequestParam(value = "filename", required = false) String filename){
        try {
            Long lId = Long.parseLong(id);
            fileService.updateFileInfo(securityUtil.getCurrentUser(), lId, comment, filename);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }


        return ResponseEntity.status(HttpStatus.OK)
                .body("File info changed");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteFile(@PathVariable String id){
        try {
            System.out.println("Начало");
            Long lId = Long.parseLong(id);
            fileService.deleteFile(securityUtil.getCurrentUser(), lId);
        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body("File deleted");
    }
}
