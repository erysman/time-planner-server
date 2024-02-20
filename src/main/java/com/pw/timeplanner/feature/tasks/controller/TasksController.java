package com.pw.timeplanner.feature.tasks.controller;

import com.pw.timeplanner.feature.tasks.api.TasksResource;
import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.UpdateTaskDTO;
import com.pw.timeplanner.feature.tasks.service.TasksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.pw.timeplanner.core.AuthUtils.getUserIdFromToken;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TasksController implements TasksResource {

    private final TasksService tasksService;
    @Override
    public List<TaskDTO> getTasks(JwtAuthenticationToken authentication, @DateTimeFormat LocalDate day) {
        String userId = getUserIdFromToken(authentication);
        //TODO: userId must be present, throw if not
        return tasksService.getTasksByDate(userId, day);
    }

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
    public TaskDTO createTask(JwtAuthenticationToken authentication, CreateTaskDTO createTaskDTO) {
        String userId = getUserIdFromToken(authentication);
        return tasksService.createTask(userId, createTaskDTO);
    }
}
