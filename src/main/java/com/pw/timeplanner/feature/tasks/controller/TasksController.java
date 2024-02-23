package com.pw.timeplanner.feature.tasks.controller;

import com.pw.timeplanner.feature.tasks.api.TasksResource;
import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.UpdateTaskDTO;
import com.pw.timeplanner.feature.tasks.service.TasksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

import static com.pw.timeplanner.core.AuthUtils.getUserIdFromToken;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TasksController implements TasksResource {

    private final TasksService tasksService;

    @Override
    public TaskDTO getTask(JwtAuthenticationToken authentication, UUID taskId) {
        String userId = getUserIdFromToken(authentication);
        return tasksService.getTask(userId, taskId);
    }

    @Override
    public void deleteTask(JwtAuthenticationToken authentication, UUID id) {
        String userId = getUserIdFromToken(authentication);
        tasksService.deleteTask(userId, id);
    }

    @Override
    public TaskDTO updateTask(JwtAuthenticationToken authentication, UUID id, UpdateTaskDTO updateTaskDTO) {
        String userId = getUserIdFromToken(authentication);
        return tasksService.updateTask(userId, id, updateTaskDTO);
    }

    @Override
    public ResponseEntity<TaskDTO> createTask(JwtAuthenticationToken authentication, CreateTaskDTO createTaskDTO) {
        String userId = getUserIdFromToken(authentication);
        TaskDTO task = tasksService.createTask(userId, createTaskDTO);
        return ResponseEntity.created(URI.create("/"+task.getId())).body(task);
    }
}
