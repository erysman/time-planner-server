package com.pw.timeplanner.debug.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@Slf4j
public class ValidateAuthController {
    @GetMapping("/validateAuth")
    public boolean validate(JwtAuthenticationToken authentication) {
        String userId = authentication.getToken().getClaim("user_id");
        log.info("Valid token with userId: "+userId);
        return true;
    }
}
