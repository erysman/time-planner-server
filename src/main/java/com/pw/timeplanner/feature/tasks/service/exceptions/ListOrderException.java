package com.pw.timeplanner.feature.tasks.service.exceptions;

import org.zalando.problem.Status;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import java.util.List;

public class ListOrderException extends ConstraintViolationProblem {

    public ListOrderException(String message) {
        super(Status.BAD_REQUEST, List.of(new Violation("", message)));
    }

}