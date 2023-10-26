package com.pw.timeplanner.api;

import com.pw.timeplanner.api.dto.TaskDTO;
import com.pw.timeplanner.api.dto.TaskUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    Optional<TaskDTO> getTask(JwtAuthenticationToken authentication,
                              @PathVariable("id") UUID id);

    @PatchMapping("/{id}")
    Optional<TaskDTO> updateTask(JwtAuthenticationToken authentication,
                       @PathVariable("id") UUID id,
                       @RequestBody TaskUpdateDTO taskUpdateDTO);

}
