package com.pw.timeplanner.feature.tasks.controller;

import com.pw.timeplanner.api.TasksResource;
import com.pw.timeplanner.api.dto.TaskDTO;
import com.pw.timeplanner.api.dto.TaskUpdateDTO;
import com.pw.timeplanner.feature.tasks.service.TasksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TasksController implements TasksResource {

    private final TasksService tasksService;

    @Override
    public List<TaskDTO> getTasks(JwtAuthenticationToken authentication, @DateTimeFormat LocalDate day) {
        String userId = authentication.getToken().getClaim("user_id");
        //TODO: userId must be present, throw if not
        return tasksService.getTasks(userId, day);
    }

    @Override
    public Optional<TaskDTO> getTask(JwtAuthenticationToken authentication, UUID taskId) {
        String userId = authentication.getToken().getClaim("user_id");
        return tasksService.getTask(userId, taskId);
    }

    @Override
    public Optional<TaskDTO> updateTask(JwtAuthenticationToken authentication, UUID id, TaskUpdateDTO taskUpdateDTO) {
        String userId = authentication.getToken().getClaim("user_id");
        return tasksService.updateTask(userId, id, taskUpdateDTO);
    }
}
