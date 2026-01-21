package com.bacoge.constructionmaterial.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MultipartException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value"
            ));
            
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            "Validation failed",
            "One or more fields have validation errors",
            HttpStatus.BAD_REQUEST.value(),
            errors
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class })
    public ResponseEntity<ErrorResponse> handleRequestParamErrors(Exception ex) {
        Map<String, String> details = new HashMap<>();
        details.put("parameter", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            "Bad Request",
            "Invalid or missing request parameter",
            HttpStatus.BAD_REQUEST.value(),
            details
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ ConstraintViolationException.class })
    public ResponseEntity<ErrorResponse> handleConstraintViolations(ConstraintViolationException ex) {
        Map<String, String> details = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                v -> v.getPropertyPath() != null ? v.getPropertyPath().toString() : "property",
                v -> v.getMessage() != null ? v.getMessage() : "invalid"
            ));
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            "Validation failed",
            "One or more fields have validation errors",
            HttpStatus.BAD_REQUEST.value(),
            details
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ MultipartException.class })
    public ResponseEntity<ErrorResponse> handleMultipartErrors(MultipartException ex) {
        Map<String, String> details = new HashMap<>();
        details.put("multipart", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            "Bad Request",
            "Invalid multipart/form-data",
            HttpStatus.BAD_REQUEST.value(),
            details
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ IllegalArgumentException.class })
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> details = new HashMap<>();
        details.put("argument", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            "Bad Request",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            details
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ DataIntegrityViolationException.class })
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        Map<String, String> details = new HashMap<>();
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        details.put("integrity", msg);
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            "Bad Request",
            "Violation d'intégrité des données",
            HttpStatus.BAD_REQUEST.value(),
            details
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            "Internal Server Error",
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            null
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    public static class ErrorResponse {
        private final LocalDateTime timestamp;
        private final String error;
        private final String message;
        private final int status;
        private final Map<String, String> details;
        
        public ErrorResponse(LocalDateTime timestamp, String error, String message, int status, Map<String, String> details) {
            this.timestamp = timestamp;
            this.error = error;
            this.message = message;
            this.status = status;
            this.details = details;
        }
        
        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public int getStatus() { return status; }
        public Map<String, String> getDetails() { return details; }
    }
}
