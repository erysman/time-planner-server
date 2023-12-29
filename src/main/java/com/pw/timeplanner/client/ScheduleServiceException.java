package com.pw.timeplanner.client;

public class ScheduleServiceException extends RuntimeException {

    public ScheduleServiceException(String code, String headers) {
        super("Exception on request to scheduling service, http code:"+code +" headers:"+headers);
    }
}

