package com.apigateway.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoRoute(NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND,
                "Not Found",
                "No route defined for: " + ex.getResourcePath(),
                request.getRequestURI());
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleConnectionFailure(ResourceAccessException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE,
                "Service Unavailable",
                "Unable to connect to the upstream service. Please try again later.",
                request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleNoInstances(IllegalStateException ex, HttpServletRequest request) {
        String message = ex.getMessage();

        if (message != null && message.contains("No instances available")) {
            return buildResponse(HttpStatus.SERVICE_UNAVAILABLE,
                    "Service Unavailable",
                    "The requested service has no available instances. Please try again later.",
                    request.getRequestURI());
        }

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(Instant.now(), status.value(), error, message, path));
    }
}
