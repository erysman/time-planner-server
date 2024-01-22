package com.pw.timeplanner.core;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class AuthUtils {
    public static String getUserIdFromToken(JwtAuthenticationToken authentication) {
        return authentication.getToken().getClaim("user_id");
    }
}
