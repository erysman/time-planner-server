package com.pw.timeplanner.feature.user.service;

import com.pw.timeplanner.feature.tasks.api.projectDto.ProjectDTO;
import com.pw.timeplanner.feature.tasks.service.ProjectService;
import com.pw.timeplanner.feature.user.api.UserInfoDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final ProjectService projectService;

    public UserInfoDTO getUserInfo(String userId, JwtAuthenticationToken authentication) {
        Optional<ProjectDTO> defaultProject = projectService.getDefaultProject(userId);
        return UserInfoDTO.builder()
                .isInitialized(defaultProject.isPresent())
                .build();
    }

    public UserInfoDTO initializeUser(String userId) {
        projectService.createDefaultProject(userId);
        return UserInfoDTO.builder()
                .isInitialized(true)
                .build();
    }
}
