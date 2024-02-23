package com.pw.timeplanner.feature.tasks.controller;

import com.pw.timeplanner.feature.tasks.api.ProjectsResource;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.CreateProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.ProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.UpdateProjectDTO;
import com.pw.timeplanner.feature.tasks.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
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
    public ResponseEntity<ProjectDTO> createProject(JwtAuthenticationToken authentication, CreateProjectDTO createProjectDTO) {
        String userId = getUserIdFromToken(authentication);
        ProjectDTO project = projectService.createProject(userId, createProjectDTO);
        return ResponseEntity.created(URI.create("/"+project.getId())).body(project);
    }

    @Override
    public List<UUID> getTasksProjectOrder(JwtAuthenticationToken authentication, UUID id) {
        String userId = getUserIdFromToken(authentication);
        return projectService.getTasksOrderForProject(userId, id);
    }

    @Override
    public List<UUID> updateTasksProjectOrder(JwtAuthenticationToken authentication, UUID id, List<UUID> positions) {
        String userId = getUserIdFromToken(authentication);
        return projectService.updateTasksOrderForProject(userId, id, positions);
    }
}
