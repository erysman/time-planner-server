package com.pw.timeplanner.feature.tasks.api;

import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.UpdateTaskDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

//@Validated
@RequestMapping(TasksResource.RESOURCE_PATH)
public interface TasksResource {

    String RESOURCE_PATH = "/tasks";

    @GetMapping
    @Operation(summary = "Get tasks", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Unauthorized")
    })
    List<TaskDTO> getTasks(JwtAuthenticationToken authentication,
                           @RequestParam("day") LocalDate day);

    @GetMapping("/{id}")
    TaskDTO getTask(JwtAuthenticationToken authentication,
                              @PathVariable("id") UUID id);

    @DeleteMapping("/{id}")
    void deleteTask(JwtAuthenticationToken authentication,
                              @PathVariable("id") UUID id);

    @PatchMapping("/{id}")
    TaskDTO updateTask(JwtAuthenticationToken authentication,
                       @PathVariable("id") UUID id,
                       @RequestBody @Validated UpdateTaskDTO updateTaskDTO);

    @PostMapping
    TaskDTO createTask(JwtAuthenticationToken authentication,
                                 @RequestBody @Valid CreateTaskDTO createTaskDTO);

}
