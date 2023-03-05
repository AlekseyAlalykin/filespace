package org.filespace.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Response {
    public static Map<String, Object> build(HttpStatus status, String message){
        Map<String, Object> response = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        response.put("timestamp",sdf.format(new Date()));
        response.put("status", status.value());
        response.put("message", message);

        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        response.put("path", builder.build().getPath());
        return response;
    }
}
