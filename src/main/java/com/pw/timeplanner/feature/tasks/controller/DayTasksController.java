package com.pw.timeplanner.feature.tasks.controller;

import com.pw.timeplanner.feature.tasks.api.DayTasksResource;
import com.pw.timeplanner.feature.tasks.api.dto.ScheduleInfoDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.service.DayTasksService;
import com.pw.timeplanner.feature.tasks.service.ScheduleService;
import com.pw.timeplanner.feature.tasks.service.TasksService;
import com.pw.timeplanner.feature.tasks.service.exceptions.DataConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DayTasksController implements DayTasksResource {

    private final TasksService tasksService;
    private final DayTasksService dayTasksService;
    private final ScheduleService scheduleService;

    @Override
    public List<TaskDTO> getDayTasks(JwtAuthenticationToken authentication, @DateTimeFormat LocalDate day) {
        String userId = authentication.getToken().getClaim("user_id");
        //TODO: userId must be present, throw if not
        return tasksService.getTasks(userId, day);
    }

    @Override
    public List<UUID> getTasksDayOrder(JwtAuthenticationToken authentication, LocalDate day) {
        String userId = authentication.getToken().getClaim("user_id");
        return dayTasksService.getTasksOrder(userId, day);
    }

    @Override
    public List<UUID> updateTasksDayOrder(JwtAuthenticationToken authentication, LocalDate day, List<UUID> tasksOrder) {
        String userId = authentication.getToken().getClaim("user_id");
        try {
            return dayTasksService.updateTasksOrder(userId, day, tasksOrder);
        } catch (DataConflictException e) {
            log.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @Override
    public ScheduleInfoDTO getAutoScheduleInfo(JwtAuthenticationToken authentication, LocalDate day) {
        String userId = authentication.getToken().getClaim("user_id");
        return scheduleService.getInfo(userId, day);
    }

    @Override
    public void schedule(JwtAuthenticationToken authentication, LocalDate day) {
        String userId = authentication.getToken().getClaim("user_id");
        scheduleService.schedule(userId, day);
    }

    @Override
    public void revokeSchedule(JwtAuthenticationToken authentication, LocalDate day) {
        String userId = authentication.getToken().getClaim("user_id");
        scheduleService.revokeSchedule(userId, day);
    }
}
