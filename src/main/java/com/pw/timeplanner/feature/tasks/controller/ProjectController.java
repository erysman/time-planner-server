package com.pw.timeplanner.feature.tasks.controller;

import com.pw.timeplanner.feature.tasks.api.ProjectsResource;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.CreateProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.ProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.UpdateProjectDTO;
import com.pw.timeplanner.feature.tasks.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.pw.timeplanner.core.AuthUtils.getUserIdFromToken;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProjectController implements ProjectsResource {

    private final ProjectService projectService;

    @Override
    public List<ProjectDTO> getProjects(JwtAuthenticationToken authentication) {
        String userId = getUserIdFromToken(authentication);
        return projectService.getProjects(userId);
    }

    @Override
    public List<TaskDTO> getProjectTasks(JwtAuthenticationToken authentication, UUID id) {
        String userId = getUserIdFromToken(authentication);
        return projectService.getProjectTasks(userId, id);
    }

    @Override
    public ProjectDTO getProject(JwtAuthenticationToken authentication, UUID id) {
        String userId = getUserIdFromToken(authentication);
        return projectService.getProject(userId, id);
    }

    @Override
    public void deleteProject(JwtAuthenticationToken authentication, UUID id) {
        String userId = getUserIdFromToken(authentication);
        projectService.deleteProject(userId, id);
    }

    @Override
    public ProjectDTO updateProject(JwtAuthenticationToken authentication, UUID id, UpdateProjectDTO updateProjectDTO) {
        String userId = getUserIdFromToken(authentication);
        return projectService.updateProject(userId, id, updateProjectDTO);
    }

    @Override
    public ProjectDTO createProject(JwtAuthenticationToken authentication, CreateProjectDTO createProjectDTO) {
        String userId = getUserIdFromToken(authentication);
        return projectService.createProject(userId, createProjectDTO);
    }
}
