
package com.example.bookrentalsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex) {

        HttpStatusCode status = ex.getStatusCode();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", status.value());

        HttpStatus httpStatus = HttpStatus.resolve(status.value());

        response.put(
                "error",
                httpStatus != null
                        ? httpStatus.getReasonPhrase()
                        : "Request Failed"
        );

        response.put(
                "message",
                ex.getReason() != null
                        ? ex.getReason()
                        : "The request could not be completed."
        );

        return ResponseEntity.status(status).body(response);
    }
}
