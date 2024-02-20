package com.pw.timeplanner.core.validation;

import com.pw.timeplanner.config.TasksProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class DurationMinValidator implements ConstraintValidator<DurationMinValid, Integer> {

    @Autowired
    public DurationMinValidator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private final ApplicationContext applicationContext;
    private TasksProperties tasksProperties;

    public void initialize(DurationMinValid parameters) {
        Class<TasksProperties> beanClass = parameters.tasksProperties();
        tasksProperties = applicationContext.getBean(beanClass);
    }

    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        ((ConstraintValidatorContextImpl)constraintValidatorContext).addMessageParameter("minDuration", tasksProperties.getMinDurationMinutes());
        return value == null || value >= tasksProperties.getMinDurationMinutes();
    }
}