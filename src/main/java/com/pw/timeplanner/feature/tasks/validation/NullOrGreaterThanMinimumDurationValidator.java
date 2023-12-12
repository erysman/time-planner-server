package com.pw.timeplanner.feature.tasks.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

public class NullOrGreaterThanMinimumDurationValidator implements ConstraintValidator<NullOrGreaterThanMinimumDuration, Integer> {

    @Value("${features.tasks.min-duration-minutes}")
    private int minDurationMinutes;

    public void initialize(NullOrGreaterThanMinimumDuration parameters) {
        // Nothing to do here
    }

    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        return value == null || value >= minDurationMinutes;
    }
}