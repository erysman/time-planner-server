package com.pw.timeplanner.feature.tasks.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( {ElementType.FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = NullOrGreaterThanMinimumDurationValidator.class)
public @interface NullOrGreaterThanMinimumDuration {
    String message() default "field either has to be null or not less than min-duration-minutes param";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default {};
}