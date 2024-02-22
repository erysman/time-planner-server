package com.pw.timeplanner.feature.tasks.service.exceptions;

import org.zalando.problem.Status;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import java.util.List;

public class TimePeriodException extends ConstraintViolationProblem {
    public TimePeriodException(String fieldName) {
        super(Status.BAD_REQUEST, List.of(new Violation(fieldName, "period's start time must be before end time")));
    }
}