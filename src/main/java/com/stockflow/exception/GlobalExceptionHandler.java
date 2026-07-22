package com.stockflow.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ApiErrorCode.DUPLICATE_RESOURCE, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_CREDENTIALS, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError body = ApiError.validation(
                status.value(),
                status.getReasonPhrase(),
                ApiErrorCode.VALIDATION_FAILED,
                "Validation failed",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError body = ApiError.validation(
                status.value(),
                status.getReasonPhrase(),
                ApiErrorCode.VALIDATION_FAILED,
                "Validation failed",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError body = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                ApiErrorCode.INTERNAL_ERROR,
                "Unexpected server error",
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, ApiErrorCode code, String message,
                                           HttpServletRequest request) {
        ApiError body = ApiError.of(
                status.value(), status.getReasonPhrase(), code, message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
