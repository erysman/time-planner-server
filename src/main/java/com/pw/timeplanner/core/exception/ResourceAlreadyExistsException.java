package com.pw.timeplanner.core.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class ResourceAlreadyExistsException extends AbstractThrowableProblem {

        public ResourceAlreadyExistsException(String resource, String fieldName, String fieldValue) {
            super(null, "Resource already exists", Status.CONFLICT,
                    String.format("Resource %s with field '%s': '%s' already exists", resource, fieldName, fieldValue));
        }
}
