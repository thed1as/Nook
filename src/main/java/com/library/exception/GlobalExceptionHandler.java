package com.library.exception;

import com.library.dto.exception.ApiError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handle(NoHandlerFoundException ex) {
        ApiError error = new ApiError(404,
                "This page wasn't found " + ex.getDetailMessageCode(),
                LocalDateTime.now());
        return ResponseEntity.status(404).body(error);
    }
}
