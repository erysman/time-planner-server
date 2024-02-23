package com.pw.timeplanner.scheduling_client;

class SchedulingServerException extends RuntimeException {

    SchedulingServerException(String code, String headers) {
        super("Exception on request to scheduling service, http code:"+code +" headers:"+headers);
    }
}

