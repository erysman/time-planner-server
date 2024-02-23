package com.pw.timeplanner.feature.user.controller;

import com.pw.timeplanner.feature.user.api.UserInfoDTO;
import com.pw.timeplanner.feature.user.api.UserResource;
import com.pw.timeplanner.feature.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import static com.pw.timeplanner.core.AuthUtils.getUserIdFromToken;

@Slf4j
@RestController
@RequiredArgsConstructor
class UserController implements UserResource {

    private final UserService service;

    @Override
    public UserInfoDTO getUserInfo(JwtAuthenticationToken authentication) {
        String userId = getUserIdFromToken(authentication);
        return service.getUserInfo(userId);
    }

    @Override
    public UserInfoDTO initializeUser(JwtAuthenticationToken authentication) {
        String userId = getUserIdFromToken(authentication);
        return service.initializeUser(userId);
    }
}
