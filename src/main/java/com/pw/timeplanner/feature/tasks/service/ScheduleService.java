package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.feature.banned_ranges.service.BannedRangesService;
import com.pw.timeplanner.feature.tasks.api.dto.ScheduleInfoDTO;
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.repository.ProjectsRepository;
import com.pw.timeplanner.feature.tasks.repository.TasksRepository;
import com.pw.timeplanner.scheduling_client.SchedulingServerClient;
import com.pw.timeplanner.scheduling_client.model.BannedRange;
import com.pw.timeplanner.scheduling_client.model.Project;
import com.pw.timeplanner.scheduling_client.model.ScheduleTasksResponse;
import com.pw.timeplanner.scheduling_client.model.ScheduledTask;
import com.pw.timeplanner.scheduling_client.model.Task;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@Slf4j
public class ScheduleService {

    private final TasksOrderService orderService;
    private final TasksRepository tasksRepository;
    private final BannedRangesService bannedRangesService;
    private final SchedulingServerClient client;
    private final TasksProperties properties;

    public ScheduleInfoDTO getInfo(String userId, LocalDate day) {
        int autoScheduledTasksCount = tasksRepository.countAutoScheduledTasks(userId, day);
        return new ScheduleInfoDTO(autoScheduledTasksCount > 0);
    }

    @Transactional
    public void schedule(String userId, LocalDate day) {
        log.info("Scheduling tasks for user: " + userId + " and day: " + day);
        List<TaskEntity> taskEntities = tasksRepository.findAndLockAllByUserIdAndStartDayWithProjects(userId, day);
        List<ProjectEntity> projectEntities = taskEntities.stream()
                .map(TaskEntity::getProject)
                .distinct()
                .toList();
        List<Task> tasks = getTasks(taskEntities);
        List<Project> projects = getProjects(projectEntities);
        List<BannedRange> bannedRanges = getBannedRanges(userId);
        try {
            ScheduleTasksResponse scheduledTasksResponse = client.scheduleTasks(tasks, projects, bannedRanges);
            updateScheduledTasks(day, userId, scheduledTasksResponse, taskEntities);
        } catch (Exception e) {
            log.error("Error while trying to get schedule from scheduling service", e);
            //TODO: RETRY
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Transactional
    public void updateScheduledTasks(LocalDate day, String userId, ScheduleTasksResponse scheduledTasksResponse, List<TaskEntity> taskEntities) {
        List<ScheduledTask> scheduledTasks = scheduledTasksResponse.getScheduledTasks();
        UUID runId = scheduledTasksResponse.getRunId();

        taskEntities.stream()
                .filter(taskEntity -> taskEntity.getStartTime() == null)
                .forEach(taskEntity -> {
                    scheduledTasks.stream().filter(scheduledTask -> scheduledTask.getId().equals(taskEntity.getId()))
                            .findFirst()
                            .ifPresent(scheduledTask -> {
                                taskEntity.setAutoScheduled(true);
                                taskEntity.setScheduleRunId(runId);
                                taskEntity.setDayOrder(null);
                                LocalTime newStartTime = numberToTime(scheduledTask.getStartTime());
                                taskEntity.setStartTime(newStartTime);
                                if (taskEntity.getDurationMin() == null) {
                                    taskEntity.setDurationMin(properties.getDefaultDurationMinutes());
                                }
                            });
                });

//        scheduledTasks.forEach(scheduledTask -> {
//            Optional<TaskEntity> entity = taskEntities.stream()
//                    .filter(taskEntity -> taskEntity.getId()
//                            .equals(scheduledTask.getId()))
//                    .findFirst();
//            entity.ifPresent(taskEntity -> {
//                if (taskEntity.getStartTime() == null) {
//                    taskEntity.setAutoScheduled(true);
//                    taskEntity.setScheduleRunId(runId);
//                    taskEntity.setDayOrder(null);
//                    LocalTime newStartTime = numberToTime(scheduledTask.getStartTime());
//                    taskEntity.setStartTime(newStartTime);
//                }
//                if (taskEntity.getDurationMin() == null) {
//                    taskEntity.setDurationMin(properties.getDefaultDurationMinutes());
//                }
//            });
//        });
        //update day order for all tasks in the day
        orderService.updateDayOrder(userId, day);
    }

//    private void updateScheduledTasks(String userId, ScheduleTasksResponse scheduledTasksResponse, List<TaskEntity> taskEntities) {
//        List<ScheduledTask> scheduledTasks = scheduledTasksResponse.getScheduledTasks();
//        UUID runId = scheduledTasksResponse.getRunId();
//        scheduledTasks.forEach(scheduledTask -> {
//            Optional<TaskEntity> entity = taskEntities.stream()
//                    .filter(taskEntity -> taskEntity.getId()
//                            .equals(scheduledTask.getId()))
//                    .findFirst();
//            entity.ifPresent(taskEntity -> {
//                if (taskEntity.getStartTime() == null) {
//                    taskEntity.setAutoScheduled(true);
//                    taskEntity.setScheduleRunId(runId);
//                    LocalTime newStartTime = numberToTime(scheduledTask.getStartTime());
//                    orderService.updateDayOrder(userId, taskEntity, null, JsonNullable.of(newStartTime));
//                    taskEntity.setStartTime(newStartTime);
//                }
//                if (taskEntity.getDurationMin() == null) {
//                    taskEntity.setDurationMin(properties.getDefaultDurationMinutes());
//                }
//            });
//        });
//    }

    private List<Task> getTasks(List<TaskEntity> taskEntities) {
        return taskEntities.stream()
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
    }

    private List<Project> getProjects(List<ProjectEntity> projectEntities) {
        return projectEntities.stream()
                .map(e -> Project.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .timeRangeStart(timeToNumber(e.getScheduleStartTime()))
                        .timeRangeEnd(getTimeRangeEnd(e.getScheduleEndTime()))
                        .build())
                .toList();
    }

    private List<BannedRange> getBannedRanges(String userId) {
        return bannedRangesService.getBannedRanges(userId)
                .stream()
                .map(e -> BannedRange.builder()
                        .id(e.getId())
                        .timeRangeStart(timeToNumber(e.getStartTime()))
                        .timeRangeEnd(getTimeRangeEnd(e.getEndTime()))
                        .build())
                .toList();
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

    private Double getTimeRangeEnd(LocalTime end) {
        if (end.getHour() == 23 && end.getMinute() == 59) {
            return 24.0;
        }
        return timeToNumber(end);
    }

    private double getDurationHours(TaskEntity e) {
        if (e.getDurationMin() != null) return e.getDurationMin() / 60.0;
        return this.properties.getDefaultDurationMinutes() / 60.0;
    }

    @Transactional
    public void revokeSchedule(String userId, LocalDate day) {
        log.info("Revoking schedule for user: " + userId + " and day: " + day);
        List<TaskEntity> taskEntities = tasksRepository.findAndLockAllByUserIdAndStartDay(userId, day);
        taskEntities.stream()
                .filter(TaskEntity::getAutoScheduled)
                .forEach(taskEntity -> {
                    taskEntity.setStartTime(null);
                    taskEntity.setAutoScheduled(false);
                    taskEntity.setScheduleRunId(null);
                });
        //set day order for all tasks without start time in the day
        orderService.setMissingDayOrder(taskEntities);
    }


//    @Transactional
//    public void revokeSchedule(String userId, LocalDate day) {
//        log.info("Revoking schedule for user: " + userId + " and day: " + day);
//        List<TaskEntity> taskEntities = tasksRepository.findAndLockAllByUserIdAndStartDay(userId, day);
//        taskEntities.stream()
//                .filter(TaskEntity::getAutoScheduled)
//                .forEach(taskEntity -> {
//                    orderService.updateDayOrder(userId, taskEntity, null, JsonNullable.of(null));
//                    taskEntity.setStartTime(null);
//                    taskEntity.setAutoScheduled(false);
//                });
//    }
}
