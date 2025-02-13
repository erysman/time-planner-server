package com.pw.timeplanner.feature.tasks.controller;

import com.pw.timeplanner.feature.tasks.api.DayTasksResource;
import com.pw.timeplanner.feature.tasks.api.dto.ScheduleInfoDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.service.ScheduleService;
import com.pw.timeplanner.feature.tasks.service.TasksDayOrderService;
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
public class DayTasksController implements DayTasksResource {

    private final TasksService tasksService;
    private final TasksDayOrderService tasksOrderService;
    private final ScheduleService scheduleService;

    @Override
    public List<TaskDTO> getDayTasks(JwtAuthenticationToken authentication, @DateTimeFormat LocalDate day) {
        String userId = getUserIdFromToken(authentication);
        return tasksService.getTasksByDate(userId, day);
    }

    @Override
    public List<UUID> getTasksDayOrder(JwtAuthenticationToken authentication, LocalDate day) {
        String userId = getUserIdFromToken(authentication);
        return tasksOrderService.getOrder(userId, day);
    }

    @Override
    public List<UUID> updateTasksDayOrder(JwtAuthenticationToken authentication, LocalDate day, List<UUID> tasksOrder) {
        String userId = getUserIdFromToken(authentication);
        return tasksOrderService.reorder(userId, day, tasksOrder);

    }

    @Override
    public ScheduleInfoDTO getAutoScheduleInfo(JwtAuthenticationToken authentication, LocalDate day) {
        String userId = getUserIdFromToken(authentication);
        return scheduleService.getInfo(userId, day);
    }

    @Override
    public void schedule(JwtAuthenticationToken authentication, LocalDate day) {
        String userId = getUserIdFromToken(authentication);
        scheduleService.schedule(userId, day);
    }

    @Override
    public void revokeSchedule(JwtAuthenticationToken authentication, LocalDate day) {
        String userId = getUserIdFromToken(authentication);
        scheduleService.revokeSchedule(userId, day);
    }
}
