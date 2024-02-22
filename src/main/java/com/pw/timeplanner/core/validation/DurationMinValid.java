package com.pw.timeplanner.core.validation;

import com.pw.timeplanner.config.TasksProperties;
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
@Constraint(validatedBy = DurationMinValidator.class)
public @interface DurationMinValid {
    String message() default "must be greater than or equal to {minDuration} minutes";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default {};

    /**
     * Class of the bean to be loaded from Spring context.
     */
    Class<TasksProperties> tasksProperties();
}