package com.songservice.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(FieldError::getField))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, this::mapEntryToMapValue));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("Validation error", details, String.valueOf(HttpStatus.BAD_REQUEST.value())));
    }

    private String mapEntryToMapValue(Map.Entry<String, List<FieldError>> entry) {
        List<FieldError> errors = entry.getValue();

        return errors.stream()
                .filter(err -> err.getCode() != null && err.getCode().startsWith("NotNull"))
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse(errors.getFirst().getDefaultMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value '%s' for %s. Must be a positive integer".formatted(ex.getValue(), ex.getName().toUpperCase());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(message, null, String.valueOf(HttpStatus.BAD_REQUEST.value())));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(this::constrainViolationToMessage)
                .collect(Collectors.joining());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(message, null, String.valueOf(HttpStatus.BAD_REQUEST.value())));
    }

    private String constrainViolationToMessage(ConstraintViolation<?> cv) {
        String messageTemplate = cv.getMessageTemplate();
        Object invalidValue = cv.getInvalidValue();

        return messageTemplate.formatted(invalidValue);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponseDto(ex.getReason(), null, String.valueOf(ex.getStatusCode().value())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("An unexpected error occurred: " + ex.getMessage(), null, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())));
    }
}
