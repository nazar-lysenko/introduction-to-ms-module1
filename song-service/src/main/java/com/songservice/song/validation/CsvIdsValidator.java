package com.songservice.song.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CsvIdsValidator implements ConstraintValidator<ValidCsvIds, String> {
    private static final String CSV_STRING_LENGTH_VIOLATION_MESSAGE_TEMPLATE =
            "CSV string is too long: received %s characters, maximum allowed is %s";
    private static final int CSV_STRING_MAX_LENGTH = 200;
    private static final String CSV_STRING_SEPARATOR = ",";
    private static final String CSV_STRING_PATTERN = "^[1-9]\\d*$";
    private static final String CSV_STRING_INVALID_FORMAT_VIOLATION_MESSAGE_TEMPLATE =
            "Invalid ID format: '%s'. Only positive integers are allowed";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value.length() > CSV_STRING_MAX_LENGTH) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    CSV_STRING_LENGTH_VIOLATION_MESSAGE_TEMPLATE.formatted(value.length(), CSV_STRING_MAX_LENGTH)
            ).addConstraintViolation();
            return false;
        }

        for (String token : value.split(CSV_STRING_SEPARATOR)) {
            String trimmed = token.trim();
            if (!trimmed.matches(CSV_STRING_PATTERN)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        CSV_STRING_INVALID_FORMAT_VIOLATION_MESSAGE_TEMPLATE.formatted(trimmed)
                ).addConstraintViolation();

                return false;
            }
        }

        return true;
    }
}
