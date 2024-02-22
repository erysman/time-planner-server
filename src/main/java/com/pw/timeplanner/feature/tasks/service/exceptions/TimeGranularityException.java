package com.pw.timeplanner.feature.tasks.service.exceptions;

import org.zalando.problem.Status;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import java.util.List;

public class TimeGranularityException extends ConstraintViolationProblem {
    public TimeGranularityException(String fieldName, Integer timeGranularityMinutes) {
        super(Status.BAD_REQUEST, List.of(new Violation(fieldName, String.format("must be multiple of '%s'", timeGranularityMinutes))));
    }
}