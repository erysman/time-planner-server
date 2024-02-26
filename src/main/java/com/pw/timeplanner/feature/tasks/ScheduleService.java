package com.pw.timeplanner.feature.tasks;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.feature.banned_ranges.BannedRangesService;
import com.pw.timeplanner.feature.projects.ProjectService;
import com.pw.timeplanner.feature.projects.dto.ProjectDTO;
import com.pw.timeplanner.feature.tasks.api.dto.ScheduleInfoDTO;
import com.pw.timeplanner.scheduling_client.SchedulingServerClient;
import com.pw.timeplanner.scheduling_client.model.BannedRange;
import com.pw.timeplanner.scheduling_client.model.Project;
import com.pw.timeplanner.scheduling_client.model.ScheduleTasksResponse;
import com.pw.timeplanner.scheduling_client.model.ScheduledTask;
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

    private final TasksDayOrderService orderService;
    private final TasksRepository tasksRepository;
    private final BannedRangesService bannedRangesService;
    private final SchedulingServerClient client;
    private final TasksProperties properties;
    private final TimeConverter timeConverter;
    private final ProjectService projectService;

    public ScheduleInfoDTO getInfo(String userId, LocalDate day) {
        int autoScheduledTasksCount = tasksRepository.countAutoScheduledTasks(userId, day);
        return new ScheduleInfoDTO(autoScheduledTasksCount > 0);
    }

    @Transactional
    public void schedule(String userId, LocalDate day) {
        log.info("Scheduling tasks for user: " + userId + " and day: " + day);
        List<Task> taskEntities = tasksRepository.findAndLockAllByUserIdAndStartDayWithProjects(userId, day);
        if (taskEntities.isEmpty()) {
            return;
        }
        List<UUID> projectIds = taskEntities.stream()
                .map(Task::getProjectId)
                .distinct()
                .toList();
        List<ProjectDTO> projectDTOs = projectService.getProjectsByIds(userId, projectIds);
        List<com.pw.timeplanner.scheduling_client.model.Task> tasks = prepareTasks(taskEntities);
        List<Project> projects = prepareProjects(projectDTOs);
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
    public void revokeSchedule(String userId, LocalDate day) {
        log.info("Revoking schedule for user: " + userId + " and day: " + day);
        List<Task> taskEntities = tasksRepository.findAndLockAllByUserIdAndStartDay(userId, day);
        taskEntities.stream()
                .filter(Task::getAutoScheduled)
                .forEach(Task::unschedule);
        //set day order for all tasks without start time in the day
        orderService.updateOrder(userId, day);
    }

    private void updateScheduledTasks(LocalDate day, String userId, ScheduleTasksResponse scheduledTasksResponse,
                                      List<Task> taskEntities) {
        List<ScheduledTask> scheduledTasks = scheduledTasksResponse.getScheduledTasks();
        UUID runId = scheduledTasksResponse.getRunId();

        taskEntities.stream()
                .filter(taskEntity -> taskEntity.getStartTime() == null)
                .forEach(taskEntity -> {
                    scheduledTasks.stream()
                            .filter(scheduledTask -> scheduledTask.getId()
                                    .equals(taskEntity.getId()))
                            .findFirst()
                            .ifPresent(scheduledTask -> {
                                LocalTime newStartTime = timeConverter.numberToTime(scheduledTask.getStartTime());
                                taskEntity.schedule(runId, newStartTime);
                                if (taskEntity.getDurationMin() == null) {
                                    taskEntity.setDurationMin(properties.getDefaultDurationMinutes());
                                }
                            });
                });
        //update day order for all tasks in the day
        orderService.updateOrder(userId, day);
    }

    private List<com.pw.timeplanner.scheduling_client.model.Task> prepareTasks(List<Task> taskEntities) {
        return taskEntities.stream()
                .map(task -> com.pw.timeplanner.scheduling_client.model.Task.builder()
                        .id(task.getId())
                        .name(task.getName())
                        .startTime(timeConverter.timeToNumber(task.getStartTime()))
                        .duration(getDurationHours(task))
                        .priority(getPriority(task))
                        .projectId(task.getProjectId())
                        .build())
                .toList();
    }

    private int getPriority(Task t) {
        if (t.getIsImportant() && t.getIsUrgent()) return 1;
        if (t.getIsImportant()) return 2;
        if (t.getIsUrgent()) return 3;
        return 4;
    }

    private List<Project> prepareProjects(List<ProjectDTO> projectEntities) {
        return projectEntities.stream()
                .map(project -> Project.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .timeRangeStart(timeConverter.timeToNumber(project.getScheduleStartTime()))
                        .timeRangeEnd(timeConverter.getTimeRangeEnd(project.getScheduleEndTime()))
                        .build())
                .toList();
    }

    private List<BannedRange> prepareBannedRanges(String userId) {
        return bannedRangesService.getBannedRanges(userId)
                .stream()
                .map(range -> BannedRange.builder()
                        .id(range.getId())
                        .timeRangeStart(timeConverter.timeToNumber(range.getStartTime()))
                        .timeRangeEnd(timeConverter.getTimeRangeEnd(range.getEndTime()))
                        .build())
                .toList();
    }

    private double getDurationHours(Task e) {
        if (e.getDurationMin() != null) return e.getDurationMin() / 60.0;
        return this.properties.getDefaultDurationMinutes() / 60.0;
    }
}
