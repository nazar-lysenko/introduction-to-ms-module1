package com.resourceservice.exception;

import com.resourceservice.resource.Constants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        StringBuilder message = new StringBuilder("Invalid file format: '%s'.");

        boolean isAudioType = ex.getSupportedMediaTypes().stream()
                .anyMatch(type -> Constants.RESOURCE_SUPPORTED_MEDIA_TYPE.equals(type.toString()));

        if (isAudioType) {
            message.append(" ").append("Only MP3 files are allowed");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(message.toString().formatted(ex.getContentType()), null, String.valueOf(HttpStatus.BAD_REQUEST.value())));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        StringBuilder messageTemplate = new StringBuilder("Invalid value '%s' for %s.");

        if (isValidationRequiredTypeInteger(ex.getRequiredType())) {
            messageTemplate.append(" ").append("Must be a positive integer");
        }

        String message = messageTemplate.toString().formatted(ex.getValue(), ex.getName().toUpperCase());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(message, null, String.valueOf(HttpStatus.BAD_REQUEST.value())));
    }

    private boolean isValidationRequiredTypeInteger(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        return Stream.of(Long.class)
                .map(Class::getName)
                .anyMatch(c -> c.equals(clazz.getName()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponseDto(ex.getReason(), null, String.valueOf(ex.getStatusCode().value())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("An unexpected error occurred", null, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())));
    }
}
