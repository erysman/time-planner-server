package com.pw.timeplanner.debug.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@Slf4j
public class ValidateAuthController {
    @GetMapping("/validateAuth")
    public boolean validate(Principal principal) {
        log.info("principal:"+principal);
        return true;
    }
}