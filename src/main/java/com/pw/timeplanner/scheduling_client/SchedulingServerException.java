package com.pw.timeplanner.scheduling_client;

public class SchedulingServerException extends RuntimeException {

    public SchedulingServerException(String code, String headers) {
        super("Exception on request to scheduling service, http code:"+code +" headers:"+headers);
    }
}

