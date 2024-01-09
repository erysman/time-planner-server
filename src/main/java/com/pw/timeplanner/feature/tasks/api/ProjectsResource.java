package com.pw.timeplanner.feature.tasks.api;

import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.CreateProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.ProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.UpdateProjectDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//@Validated
@RequestMapping(ProjectsResource.RESOURCE_PATH)
public interface ProjectsResource {

    String RESOURCE_PATH = "/projects";

    @GetMapping
    @Operation(summary = "Get projects", responses = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Unauthorized")})
    List<ProjectDTO> getProjects(JwtAuthenticationToken authentication);

    @GetMapping("/{id}")
    Optional<ProjectDTO> getProject(JwtAuthenticationToken authentication, @PathVariable("id") UUID id);

    @GetMapping("/{id}/tasks")
    @Operation(summary = "Get project tasks", responses = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Unauthorized")})
    List<TaskDTO> getProjectTasks(JwtAuthenticationToken authentication, @PathVariable("id") UUID id);

    @DeleteMapping("/{id}")
    void deleteProject(JwtAuthenticationToken authentication, @PathVariable("id") UUID id);

    @PatchMapping("/{id}")
    Optional<ProjectDTO> updateProject(JwtAuthenticationToken authentication, @PathVariable("id") UUID id,
                                       @RequestBody @Validated UpdateProjectDTO updateProjectDTO);

    @PostMapping
    ProjectDTO createProject(JwtAuthenticationToken authentication,
                             @RequestBody @Validated CreateProjectDTO createProjectDTO);

}
