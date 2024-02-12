package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.feature.banned_ranges.service.BannedRangesService;
import com.pw.timeplanner.feature.tasks.api.dto.ScheduleInfoDTO;
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class ScheduleService {
    private final TasksOrderService orderService;
    private final TasksRepository tasksRepository;
    private final BannedRangesService bannedRangesService;
    private final SchedulingServerClient client;
    private final TasksProperties properties;
    private final TimeConverter timeConverter;

    public ScheduleInfoDTO getInfo(String userId, LocalDate day) {
        int autoScheduledTasksCount = tasksRepository.countAutoScheduledTasks(userId, day);
        return new ScheduleInfoDTO(autoScheduledTasksCount > 0);
    }
    @Transactional
    public void schedule(String userId, LocalDate day) {
        log.info("Scheduling tasks for user: " + userId + " and day: " + day);
        List<TaskEntity> taskEntities = tasksRepository.findAndLockAllByUserIdAndStartDayWithProjects(userId, day);
        if(taskEntities.isEmpty()) {
            return;
        }
        List<ProjectEntity> projectEntities = taskEntities.stream()
                .map(TaskEntity::getProject)
                .distinct()
                .toList();
        List<Task> tasks = prepareTasks(taskEntities);
        List<Project> projects = prepareProjects(projectEntities);
        List<BannedRange> bannedRanges = prepareBannedRanges(userId);
        try {
            ScheduleTasksResponse scheduledTasksResponse = client.scheduleTasks(tasks, projects, bannedRanges);
            updateScheduledTasks(day, userId, scheduledTasksResponse, taskEntities);
        } catch (Exception e) {
            log.error("Error while trying to get schedule from scheduling service", e);
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
                                LocalTime newStartTime = timeConverter.numberToTime(scheduledTask.getStartTime());
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

    private List<Task> prepareTasks(List<TaskEntity> taskEntities) {
        return taskEntities.stream()
                .map(e -> Task.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .startTime(timeConverter.timeToNumber(e.getStartTime()))
                        .duration(timeConverter.getDurationHours(e))
                        .priority(e.getPriority()
                                .getValue())
                        .projectId(e.getProject()
                                .getId())
                        .build())
                .toList();
    }

    private List<Project> prepareProjects(List<ProjectEntity> projectEntities) {
        return projectEntities.stream()
                .map(e -> Project.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .timeRangeStart(timeConverter.timeToNumber(e.getScheduleStartTime()))
                        .timeRangeEnd(timeConverter.getTimeRangeEnd(e.getScheduleEndTime()))
                        .build())
                .toList();
    }

    private List<BannedRange> prepareBannedRanges(String userId) {
        return bannedRangesService.getBannedRanges(userId)
                .stream()
                .map(e -> BannedRange.builder()
                        .id(e.getId())
                        .timeRangeStart(timeConverter.timeToNumber(e.getStartTime()))
                        .timeRangeEnd(timeConverter.getTimeRangeEnd(e.getEndTime()))
                        .build())
                .toList();
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
