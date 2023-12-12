package com.pw.timeplanner.feature.tasks.controller;

import com.pw.timeplanner.feature.tasks.api.TasksResource;
import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskUpdateDTO;
import com.pw.timeplanner.feature.tasks.service.TasksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    public void deleteTask(JwtAuthenticationToken authentication, UUID id) {
        String userId = authentication.getToken().getClaim("user_id");
        if(!tasksService.deleteTask(userId, id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Optional<TaskDTO> updateTask(JwtAuthenticationToken authentication, UUID id, TaskUpdateDTO taskUpdateDTO) {
        String userId = authentication.getToken().getClaim("user_id");
        return tasksService.updateTask(userId, id, taskUpdateDTO);
    }

    @Override
    public Optional<TaskDTO> createTask(JwtAuthenticationToken authentication, CreateTaskDTO createTaskDTO) {
        String userId = authentication.getToken().getClaim("user_id");
        return tasksService.createTask(userId, createTaskDTO);
    }
}
