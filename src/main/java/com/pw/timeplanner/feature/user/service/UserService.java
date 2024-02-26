package com.pw.timeplanner.feature.user.service;

import com.pw.timeplanner.feature.projects.ProjectService;
import com.pw.timeplanner.feature.projects.dto.ProjectDTO;
import com.pw.timeplanner.feature.user.api.UserInfoDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final ProjectService projectService;

    public UserInfoDTO getUserInfo(String userId) {
        ProjectDTO defaultProject = projectService.getOrCreateDefaultProject(userId);
        return UserInfoDTO.builder()
                .isInitialized(defaultProject != null)
                .build();
    }

    public UserInfoDTO initializeUser(String userId) {
        ProjectDTO defaultProject = projectService.getOrCreateDefaultProject(userId);
        return UserInfoDTO.builder()
                .isInitialized(defaultProject != null)
                .build();
    }
}
