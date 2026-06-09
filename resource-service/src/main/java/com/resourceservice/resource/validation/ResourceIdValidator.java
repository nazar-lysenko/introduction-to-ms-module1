package com.resourceservice.resource.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ResourceIdValidator implements ConstraintValidator<ValidResourceId, Long> {
    private static final String ID_VALUE_VIOLATION_MESSAGE_TEMPLATE = "Invalid value '%s' for ID. Must be a positive integer";

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value == null || value <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ID_VALUE_VIOLATION_MESSAGE_TEMPLATE).addConstraintViolation();

            return false;
        }

        return true;
    }
}
