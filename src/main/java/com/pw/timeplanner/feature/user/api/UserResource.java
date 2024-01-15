package com.pw.timeplanner.feature.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//@Validated
@RequestMapping(UserResource.RESOURCE_PATH)
public interface UserResource {

    String RESOURCE_PATH = "/user";

    @GetMapping
    @Operation(summary = "Get user info", responses = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Unauthorized")})
    UserInfoDTO getUserInfo(JwtAuthenticationToken authentication);

    @PostMapping("/initialize")
    UserInfoDTO initializeUser(JwtAuthenticationToken authentication);

}
