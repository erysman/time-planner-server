package com.pw.timeplanner.feature.tasks.service.exceptions;

public class NullDurationMinException extends IllegalArgumentException {

    public NullDurationMinException() {
        super("Duration can't be null if startTime is present");
    }
}
                                        