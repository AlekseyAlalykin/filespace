package org.filespace.controllers;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.filespace.model.entities.File;
import org.filespace.security.SecurityUtil;
import org.filespace.services.FileService;
import org.filespace.services.IntegratedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileControllerAPI {

    @Autowired
    IntegratedService integratedService;

    @GetMapping
    public ResponseEntity getFiles(){
        List<File> files = null;

        try {
            files = integratedService.getUserFiles(SecurityUtil.getCurrentUserUsername());
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
        if (length == -1 || length > FileService.getMaxContentLength())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Content length is unknown or exceeds set limit of: "
                            + FileService.getMaxContentLength());

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (!isMultipart)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Not a multipart request");

        //List<FileItem> fileItems = request

        try {
            integratedService.saveFile(request, SecurityUtil.getCurrentUserUsername());

        } catch (FileUploadException e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("File upload error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        } catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("File upload error: " + e.getMessage());
        }
        catch (Exception e){
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Received");
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getFile(@PathVariable String id){
        List<Object> list = null;

        try {
            Long lId = Long.parseLong(id);

            list = integratedService.sendFile(SecurityUtil.getCurrentUserUsername(), lId);
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
    public ResponseEntity patchFileInfo(@PathVariable String id,
                                        @RequestParam(required = false) String comment){
        try {
            if (comment == null)
                throw new NullPointerException("No parameter \"comment\" specified");

            Long lId = Long.parseLong(id);
            integratedService.updateFileComment(SecurityUtil.getCurrentUserUsername(),lId,comment);
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
            integratedService.deleteFile(SecurityUtil.getCurrentUserUsername(), lId);
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
