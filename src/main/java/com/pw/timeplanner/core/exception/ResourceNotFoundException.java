package com.pw.timeplanner.core.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.util.UUID;

public class ResourceNotFoundException extends AbstractThrowableProblem {

        public ResourceNotFoundException(String resource, UUID id) {
            super(null, "Resource not found", Status.NOT_FOUND,  String.format("Resource %s with id %s not found", resource, id.toString()));
        }
}
