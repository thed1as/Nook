package com.library.exception;

import com.library.dto.exception.ApiError;
import com.library.dto.exception.customException.forbiden.ForbiddenUserException;
import com.library.dto.exception.customException.paymentExceptions.PaymentAlreadyExistsException;
import com.library.dto.exception.customException.paymentExceptions.PaymentFailedException;
import com.library.dto.exception.customException.paymentExceptions.PaymentNotFoundException;
import com.library.dto.exception.customException.paymentExceptions.RefundNotAllowedException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
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
        ApiError error = new ApiError(400, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handle(DataIntegrityViolationException ex) {
        ApiError error = new ApiError(409, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(409).body(error);
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiError> handle(PaymentFailedException ex) {
        ApiError error = new ApiError(402, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(402).body(error);
    }

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ResponseEntity<ApiError> handle(PaymentAlreadyExistsException ex) {
        ApiError error = new ApiError(409, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(409).body(error);
    }

    @ExceptionHandler(RefundNotAllowedException.class)
    public ResponseEntity<ApiError> handle(RefundNotAllowedException ex) {
        ApiError error = new ApiError(400, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(ForbiddenUserException.class)
    public ResponseEntity<ApiError> handle(ForbiddenUserException ex) {
        ApiError error = new ApiError(403, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(403).body(error);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiError> handle(PaymentNotFoundException ex) {
        ApiError error = new ApiError(404, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(404).body(error);
    }
}