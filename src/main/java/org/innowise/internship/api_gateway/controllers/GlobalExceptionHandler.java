package org.innowise.internship.api_gateway.controllers;

import lombok.extern.slf4j.Slf4j;
import org.innowise.internship.api_gateway.dto.errors.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.validation.ConstraintViolationException;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponseException(WebClientResponseException ex) {
        log.warn("Remote service returned error {}: {}", ex.getRawStatusCode(), ex.getMessage());

        return buildErrorResponse(
                List.of(ex.getResponseBodyAsString()),
                HttpStatus.valueOf(ex.getStatusCode().value()),
                "Remote service error"
        );
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorResponse> handleWebClientRequestException(WebClientRequestException ex) {
        log.error("Remote service unavailable: {}", ex.getMessage());

        return buildErrorResponse(
                List.of(ex.getMessage()),
                HttpStatus.SERVICE_UNAVAILABLE,
                "Remote service unavailable"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        log.info("Validation failed: {} errors", errors.size());
        errors.forEach(error -> log.debug("Validation error: {}", error));

        return buildErrorResponse(
                errors,
                HttpStatus.BAD_REQUEST,
                "Bad request"
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        log.info("Constraint violation: {} errors", errors.size());
        errors.forEach(error -> log.debug("Violation error: {}", error));

        return buildErrorResponse(
                errors,
                HttpStatus.BAD_REQUEST,
                "Bad request"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedExceptions(Exception ex) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                List.of("Unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(List<String> messages, HttpStatus status, String error) {
        ErrorResponse errorResponse = new ErrorResponse(messages, status.value(), error);
        return new ResponseEntity<>(errorResponse, status);
    }
}
