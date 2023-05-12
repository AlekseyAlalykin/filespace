package org.filespace.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.filespace.config.Response;
import org.filespace.model.entities.simplerelations.File;
import org.filespace.security.SecurityUtil;
import org.filespace.services.DiskStorageService;
import org.filespace.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.NotSupportedException;
import java.io.BufferedReader;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/files")
public class FileControllerAPI {

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private FileService fileService;

    @Autowired
    private DiskStorageService diskStorageService;

    @GetMapping
    public ResponseEntity getFiles(@RequestParam(name = "q", required = false) String query){
        List<File> files = null;

        try {
            if (query == null)
                query = "";

            files = fileService.getUserFilesByFilename(securityUtil.getCurrentUser(), query);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.build(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Something went wrong, try again later"));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(files);
    }

    @PostMapping
    public ResponseEntity postFiles(HttpServletRequest request){

        long length = request.getContentLengthLong();
        if (length == -1)
            return ResponseEntity.status(HttpStatus.LENGTH_REQUIRED)
                    .body(Response.build(HttpStatus.LENGTH_REQUIRED, "Content length is unknown"));

        if (length > diskStorageService.getMaxContentLength())
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Response.build(HttpStatus.PAYLOAD_TOO_LARGE,
                            "Content length exceeds set limit of: " + diskStorageService.getMaxContentLength()));

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        List<File> files;

        //Если мултипарт то загрузка нового файла иначе копирование существующего
        if (isMultipart){
            try {
                files = fileService.saveFileFromUser(request, securityUtil.getCurrentUser());

            } catch (IllegalStateException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Response.build(HttpStatus.BAD_REQUEST,
                                "File upload error: " + e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Response.build(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Internal server error: " + e.getMessage()));
            }

        } else {
            try {
                String contentType = request.getContentType();
                if (contentType == null)
                    throw new IllegalArgumentException("Content-type header is not present");

                Long fileId;

                if (contentType.equals(MediaType.APPLICATION_JSON_VALUE)){
                    StringBuffer stringBuffer = new StringBuffer();
                    String line = null;

                    BufferedReader reader = request.getReader();
                    while ((line = reader.readLine()) != null)
                        stringBuffer.append(line);

                    ObjectMapper objectMapper = new ObjectMapper();
                    File deserializedRequest = objectMapper.readValue(stringBuffer.toString(), File.class);
                    fileId = deserializedRequest.getId();

                    if (fileId == null)
                        throw new IllegalArgumentException("Null id no allowed");
                } else if (contentType.equals(MediaType.APPLICATION_FORM_URLENCODED_VALUE)){
                    String fileIdParam = request.getParameter("fileId");

                    fileId = Long.parseLong(fileIdParam);

                    if (fileIdParam == null)
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Response.build(HttpStatus.BAD_REQUEST,"No fileId parameter"));
                } else
                    throw new NotSupportedException("Media type isn't supported");

                File file = fileService.copyFile(securityUtil.getCurrentUser(),fileId);

                files = new LinkedList<>();
                files.add(file);
            }  catch (IllegalAccessException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));

            } catch(IllegalArgumentException e){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));

            } catch (NotSupportedException e){
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(Response.build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage()));

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Response.build(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Internal server error: " + e.getMessage()));
            }
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(files);
    }

    @GetMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity getFile(
            @PathVariable String id,
            @RequestHeader(value = "Accept", required = false) String accept){
        List<Object> list = null;
        File file = null;

        try {
            Long lId = Long.parseLong(id);

            if (accept == null)
                throw new Exception("Accept header is not present");

            if (accept.contains(MediaType.APPLICATION_JSON_VALUE))
                file = fileService.getFileJSON(securityUtil.getCurrentUser(), lId);
            else if (accept.contains(MediaType.ALL_VALUE) || accept.contains(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                list = fileService.sendFileToUser(securityUtil.getCurrentUser(), lId);
            else
                throw new NotSupportedException("Media type isn't supported");

        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        } catch (NotSupportedException e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(Response.build(HttpStatus.NOT_ACCEPTABLE, e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        if (accept.contains(MediaType.APPLICATION_JSON_VALUE))
            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(file);
        else {
            File resourceFile = (File) list.get(0);
            String filename;
            try {
                filename = URLEncoder.encode(resourceFile.getFileName(), "UTF-8").
                        replace("+", "%20");
            } catch (Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(Response.build(HttpStatus.INTERNAL_SERVER_ERROR,
                                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
            }

            byte[] array = (byte[])list.get(1);

            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                    .header(HttpHeaders.CONTENT_LENGTH, resourceFile.getSize().toString())
                    .body(array);
        }
    }

    @PatchMapping(path = "/{id}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ResponseEntity updateFileInfoConsumesURLEncoded(
            @PathVariable String id,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String filename){
        try {
            Long lId = Long.parseLong(id);
            fileService.updateFileInfo(securityUtil.getCurrentUser(), lId, description, filename);
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "File info changed"));
    }

    @PatchMapping(path = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateFileInfo(@PathVariable String id, @RequestBody File file){
        try {
            Long lId = Long.parseLong(id);
            fileService.updateFileInfo(securityUtil.getCurrentUser(), lId, file.getDescription(), file.getFileName());
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.build(HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND, e.getMessage()));
        }


        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK,"File info changed"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteFile(@PathVariable String id){
        try {
            Long lId = Long.parseLong(id);
            fileService.deleteFile(securityUtil.getCurrentUser(), lId);
        } catch (IllegalAccessException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.build(HttpStatus.FORBIDDEN, e.getMessage()));
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.build(HttpStatus.NOT_FOUND,e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.build(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(Response.build(HttpStatus.OK, "File deleted"));
    }
    
}
