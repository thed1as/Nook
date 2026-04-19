package com.library.exception;

import com.library.dto.exception.ApiError;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handle(EntityNotFoundException ex) {
        ApiError error = new ApiError(404, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(404).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handle(IllegalStateException ex) {
        ApiError error = new ApiError(409, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(409).body(error);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ApiError> handle(EntityExistsException ex) {
        ApiError error = new ApiError(409, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(409).body(error);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiError> handle(PropertyReferenceException ex) {
        ApiError error = new ApiError(409, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(409).body(error);
    }
}