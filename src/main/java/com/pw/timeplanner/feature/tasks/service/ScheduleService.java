package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.client.SchedulerClient;
import com.pw.timeplanner.client.model.Project;
import com.pw.timeplanner.client.model.ScheduleTasksResponse;
import com.pw.timeplanner.client.model.ScheduledTask;
import com.pw.timeplanner.client.model.Task;
import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.feature.tasks.api.dto.ScheduleInfoDTO;
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.repository.BannedRangeRepository;
import com.pw.timeplanner.feature.tasks.repository.ProjectsRepository;
import com.pw.timeplanner.feature.tasks.repository.TasksRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class ScheduleService {

    private final TasksOrderService orderService;
    private final TasksRepository tasksRepository;
    private final ProjectsRepository projectsRepository;
    private final BannedRangeRepository bannedRangeRepository;
    private final SchedulerClient client;
    private final TasksProperties properties;

    public ScheduleInfoDTO getInfo(String userId, LocalDate day) {
        int autoScheduledTasksCount = tasksRepository.countAutoScheduledTasks(userId, day);
        return new ScheduleInfoDTO(autoScheduledTasksCount > 0);
    }

    public void schedule(String userId, LocalDate day) {
        //Lock day data
        List<TaskEntity> taskEntities = tasksRepository.findAllByUserIdAndStartDay(userId, day);
        List<ProjectEntity> projectEntities = taskEntities.stream()
                .map(TaskEntity::getProject)
                .distinct()
                .toList();
        List<Task> tasks = taskEntities.stream()
                .map(e -> Task.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .startTime(timeToNumber(e.getStartTime()))
                        .duration(getDurationHours(e))
                        .priority(e.getPriority()
                                .getValue())
                        .projectId(e.getProject()
                                .getId())
                        .build())
                .toList();
        List<Project> projects = projectEntities.stream()
                .map(e -> Project.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .timeRangeStart(timeToNumber(e.getScheduleStartTime()))
                        .timeRangeEnd(getTimeRangeEnd(e))
                        .build())
                .toList();
        try {
            ScheduleTasksResponse scheduledTasksResponse = client.scheduleTasks(tasks, projects, List.of());
            List<ScheduledTask> scheduledTasks = scheduledTasksResponse.getScheduledTasks();
            UUID runId = scheduledTasksResponse.getRunId();
            scheduledTasks.forEach(scheduledTask -> {
                Optional<TaskEntity> entity = taskEntities.stream()
                        .filter(taskEntity -> taskEntity.getId()
                                .equals(scheduledTask.getId()))
                        .findFirst();
                entity.ifPresent(taskEntity -> {
                    if (taskEntity.getStartTime() == null) {
                        taskEntity.setAutoScheduled(true);
                        taskEntity.setScheduleRunId(runId);
                        LocalTime newStartTime = numberToTime(scheduledTask.getStartTime());
                        orderService.updateDayOrder(userId, taskEntity, null, JsonNullable.of(newStartTime));
                        taskEntity.setStartTime(newStartTime);
                    }
                    if (taskEntity.getDurationMin() == null) {
                        taskEntity.setDurationMin(properties.getDefaultDurationMinutes());
                    }
                });
            });
        } catch (Exception e) {
            log.error("Error while trying to get schedule from scheduling service", e);
            //TODO: RETRY
        }
    }

    private Double timeToNumber(LocalTime time) {
        if (time == null) return null;
        return time.getHour() + time.getMinute() / 60.0;
    }

    private LocalTime numberToTime(Double time) {
        if (time == null) return null;
        double minutes = (time - time.intValue()) * 60;
        return LocalTime.of(time.intValue(), (int) minutes);
    }

    private Double getTimeRangeEnd(ProjectEntity e) {
        if (e.getScheduleEndTime()
                .equals(LocalTime.MAX)) {
            return 24.0;
        }
        return timeToNumber(e.getScheduleEndTime());
    }

    private double getDurationHours(TaskEntity e) {
        if (e.getDurationMin() != null) return e.getDurationMin() / 60.0;
        return this.properties.getDefaultDurationMinutes() / 60.0;
    }

    public void revokeSchedule(String userId, LocalDate day) {
        List<TaskEntity> taskEntities = tasksRepository.findAllByUserIdAndStartDay(userId, day);
        taskEntities.stream().filter(TaskEntity::getAutoScheduled).forEach(taskEntity -> {
            orderService.updateDayOrder(userId, taskEntity, null, JsonNullable.of(null));
            taskEntity.setStartTime(null);
            taskEntity.setAutoScheduled(false);
        });
    }
}
