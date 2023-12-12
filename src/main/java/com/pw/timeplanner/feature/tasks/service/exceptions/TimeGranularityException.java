package com.pw.timeplanner.feature.tasks.service.exceptions;

public class TimeGranularityException extends IllegalArgumentException {

    public TimeGranularityException(String fieldName, Integer timeGranularityMinutes) {
        super("Minutes in field "+fieldName+" must be multiple of "+timeGranularityMinutes);
    }
}
                                        