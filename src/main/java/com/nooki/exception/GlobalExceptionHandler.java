package com.nooki.exception;

import com.nooki.dto.exception.ApiError;
import com.nooki.dto.exception.customException.ImageException.ImageStorageException;
import com.nooki.dto.exception.customException.LocationException.LocationException;
import com.nooki.dto.exception.customException.bookingException.BookingAccessDeniedException;
import com.nooki.dto.exception.customException.bookingException.BookingCancelException;
import com.nooki.dto.exception.customException.bookingException.BookingIllegalStateException;
import com.nooki.dto.exception.customException.bookingException.BookingNotFoundException;
import com.nooki.dto.exception.customException.forbiden.ForbiddenUserException;
import com.nooki.dto.exception.customException.listingException.ListingCancelException;
import com.nooki.dto.exception.customException.listingException.ListingIllegalStateException;
import com.nooki.dto.exception.customException.listingException.ListingNotFoundException;
import com.nooki.dto.exception.customException.listingException.ListingOccupiedException;
import com.nooki.dto.exception.customException.paymentExceptions.PaymentAlreadyExistsException;
import com.nooki.dto.exception.customException.paymentExceptions.PaymentFailedException;
import com.nooki.dto.exception.customException.paymentExceptions.PaymentNotFoundException;
import com.nooki.dto.exception.customException.paymentExceptions.RefundNotAllowedException;
import com.nooki.dto.exception.customException.reviewException.ReviewAccessDeniedException;
import com.nooki.dto.exception.customException.reviewException.ReviewIllegalStateException;
import com.nooki.dto.exception.customException.reviewException.ReviewNotFoundException;
import com.nooki.dto.exception.customException.userException.UserIllegalStateException;
import com.nooki.dto.exception.customException.userException.UserNotFoundException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.security.core.AuthenticationException;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handle(EntityNotFoundException ex) {
        ApiError error = new ApiError(404, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handle(IllegalStateException ex) {
        ApiError error = new ApiError(409, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ApiError> handle(EntityExistsException ex) {
        ApiError error = new ApiError(409, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiError> handle(PropertyReferenceException ex) {
        ApiError error = new ApiError(400, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handle(DataIntegrityViolationException ex) {
        ApiError error = new ApiError(409, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiError> handle(PaymentFailedException ex) {
        ApiError error = new ApiError(402, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(error);
    }

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ResponseEntity<ApiError> handle(PaymentAlreadyExistsException ex) {
        ApiError error = new ApiError(409, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(RefundNotAllowedException.class)
    public ResponseEntity<ApiError> handle(RefundNotAllowedException ex) {
        ApiError error = new ApiError(400, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiError> handle(PaymentNotFoundException ex) {
        ApiError error = new ApiError(404, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ForbiddenUserException.class)
    public ResponseEntity<ApiError> handle(ForbiddenUserException ex) {
        ApiError error = new ApiError(403, ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

//    BOOKING EXCEPTIONS

    @ExceptionHandler(BookingIllegalStateException.class)
    public ResponseEntity<ApiError> handle(BookingIllegalStateException e) {
        ApiError error = new ApiError(409, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BookingCancelException.class)
    public ResponseEntity<ApiError> handle(BookingCancelException e) {
        ApiError error = new ApiError(409, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiError> handle(BookingNotFoundException e) {
        ApiError error = new ApiError(404, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BookingAccessDeniedException.class)
    public ResponseEntity<ApiError> handle(BookingAccessDeniedException e) {
        ApiError error = new ApiError(409, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

//    LISTING EXCEPTIONS

    @ExceptionHandler(ListingCancelException.class)
    public ResponseEntity<ApiError> handle(ListingCancelException e) {
        ApiError error = new ApiError(409, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ListingIllegalStateException.class)
    public ResponseEntity<ApiError> handle(ListingIllegalStateException e) {
        ApiError error = new ApiError(409, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ListingNotFoundException.class)
    public ResponseEntity<ApiError> handle(ListingNotFoundException e) {
        ApiError error = new ApiError(404, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ListingOccupiedException.class)
    public ResponseEntity<ApiError> handle(ListingOccupiedException e) {
        ApiError error = new ApiError(409, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(LocationException.class)
    public ResponseEntity<ApiError> handle(LocationException e) {
        ApiError error = new ApiError(404, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

//    LISTING IMAGE EXCEPTIONS

    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<ApiError> handle(ImageStorageException e) {
        ApiError error = new ApiError(404, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

//    REVIEW Exception

    @ExceptionHandler(ReviewAccessDeniedException.class)
    public ResponseEntity<ApiError> handle(ReviewAccessDeniedException e) {
        ApiError error = new ApiError(409, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(ReviewIllegalStateException.class)
    public ResponseEntity<ApiError> handle(ReviewIllegalStateException e) {
        ApiError error = new ApiError(409, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ApiError> handle(ReviewNotFoundException e) {
        ApiError error = new ApiError(404, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

//    User exception

    @ExceptionHandler(UserIllegalStateException.class)
    public ResponseEntity<ApiError> handle(UserIllegalStateException e) {
        ApiError error = new ApiError(409, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handle(UserNotFoundException e) {
        ApiError error = new ApiError(404, e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

//    SECURITY EXCEPTION HANDLING
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handle(AuthenticationException e) {
        int status = 409;
        String message = e.getMessage();
        LocalDateTime timestamp = LocalDateTime.now();

        if(e instanceof DisabledException) {
            status = 403;
            message = e.getMessage();
        } else if (e instanceof LockedException) {
            status = 404;
            message = e.getMessage();
        } else if(e instanceof BadCredentialsException) {
            status = 401;
            message = e.getMessage() + " Wrong email or password";
        }

        return ResponseEntity.status(status).body(new ApiError(status, message, timestamp));
    }
}