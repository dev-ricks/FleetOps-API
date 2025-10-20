package com.fleetops.controller;

/**
 * Centralized controller advice that translates exceptions into standardized HTTP responses.
 * <p>
 * Typical mappings include:
 * <ul>
 *   <li>Constraint/validation violations -> 400 Bad Request</li>
 *   <li>Not found exceptions -> 404 Not Found</li>
 *   <li>Conflicts such as duplicate resources -> 409 Conflict</li>
 *   <li>Service layer exceptions -> 500 Internal Server Error</li>
 *   <li>Unhandled errors -> 500 Internal Server Error</li>
 * </ul>
 * The corresponding response bodies follow a simple error format (see ErrorResponse in OpenAPI).
 */

import com.fleetops.exception.LicensePlateAlreadyExistsException;
import com.fleetops.exception.NotFoundExceptionBase;
import com.fleetops.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    /**
     * Creates a standardized error response body.
     *
     * @param status HTTP status
     * @param error Error type description
     * @param message Error message
     * @param request Web request for additional context
     * @return Map containing error details
     */
    private Map<String, Object> createErrorResponse(HttpStatus status, String error, String message, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return body;
    }

    /**
     * Creates a standardized error response body with additional details.
     *
     * @param status HTTP status
     * @param error Error type description
     * @param message Error message
     * @param request Web request for additional context
     * @param details Additional error details
     * @return Map containing error details
     */
    private Map<String, Object> createErrorResponse(HttpStatus status, String error, String message, WebRequest request, Map<String, Object> details) {
        Map<String, Object> body = createErrorResponse(status, error, message, request);
        if (details != null && !details.isEmpty()) {
            body.putAll(details);
        }
        return body;
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, Object>> handleServiceException(ServiceException ex, WebRequest request) {
        logger.error("Service exception occurred: {}", ex.getMessage(), ex);
        Map<String, Object> body = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "A service error occurred. Please try again later.",
            request
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Unexpected runtime exception occurred: {}", ex.getMessage(), ex);
        Map<String, Object> body = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(NotFoundExceptionBase.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundExceptionBase ex, WebRequest request) {
        logger.debug("Resource not found: {}", ex.getMessage());
        Map<String, Object> body = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Not Found",
            ex.getMessage(),
            request
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(LicensePlateAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleLicensePlateConflict(LicensePlateAlreadyExistsException ex, WebRequest request) {
        logger.warn("License plate conflict: {}", ex.getMessage());
        Map<String, Object> body = createErrorResponse(
            HttpStatus.CONFLICT,
            "Conflict",
            ex.getMessage(),
            request
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        logger.warn("Data integrity violation: {}", ex.getMessage());
        Map<String, Object> body = createErrorResponse(
            HttpStatus.CONFLICT,
            "Conflict",
            "The operation conflicts with existing data constraints.",
            request
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        logger.debug("Validation failed: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                fieldError -> fieldError.getField(),
                fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                (existing, replacement) -> existing
            ));
        
        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", fieldErrors);
        
        Map<String, Object> body = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            "Request validation failed for one or more fields.",
            request,
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        logger.debug("Constraint violation: {}", ex.getMessage());
        
        Map<String, String> violations = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                v -> v.getMessage(),
                (existing, replacement) -> existing
            ));
        
        Map<String, Object> details = new HashMap<>();
        details.put("violations", violations);
        
        Map<String, Object> body = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Constraint Violation",
            "Request contains constraint violations.",
            request,
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadJson(HttpMessageNotReadableException ex, WebRequest request) {
        logger.debug("Malformed JSON request: {}", ex.getMessage());
        Map<String, Object> body = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "Malformed JSON request. Please check your request format.",
            request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        Map<String, Object> body = createErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            "Authentication is required to access this resource.",
            request
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logger.warn("Access denied: {}", ex.getMessage());
        Map<String, Object> body = createErrorResponse(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            "You do not have permission to access this resource.",
            request
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}
