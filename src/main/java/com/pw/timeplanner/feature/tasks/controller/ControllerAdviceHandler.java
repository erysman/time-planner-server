package com.pw.timeplanner.feature.tasks.controller;

import com.pw.timeplanner.feature.tasks.service.exceptions.NullDurationMinException;
import com.pw.timeplanner.feature.tasks.service.exceptions.TimeGranularityException;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@ResponseBody
public class ControllerAdviceHandler extends ResponseEntityExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({TimeGranularityException.class, NullDurationMinException.class})
    public ErrorMessage handleConflict(RuntimeException e, WebRequest request) {
        return new ErrorMessage(e.getMessage());
    }
}
