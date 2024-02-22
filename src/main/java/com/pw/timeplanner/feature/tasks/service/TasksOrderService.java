package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.repository.TasksRepository;
import com.pw.timeplanner.feature.tasks.service.exceptions.ListOrderException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class TasksOrderService {

    private final TasksRepository tasksRepository;

    public List<UUID> getTasksOrderForDay(String userId, LocalDate day) {
        log.info("Getting tasks order for user: {} and day: {}", userId, day);
        return tasksRepository.findTaskIdsOrderedByDayOrder(userId, day);
    }

    public List<UUID> getTasksOrderForProject(String userId, ProjectEntity project) {
        log.info("Getting tasks order for user: {} and project: {}", userId, project.getId());
        return tasksRepository.findTaskIdsOrderedByProject(userId, project);
    }

    @Transactional
    public List<UUID> reorderTasksForProject(String userId, ProjectEntity project, List<UUID> newTasksOrder) {
        log.info("Reordering tasks for user: {} and project: {} with new order: {}", userId, project.getId(), newTasksOrder);
        Set<TaskEntity> tasksWithProjectOrder = tasksRepository.findAndLockTasksWithProjectOrder(userId, project);
        validateTaskOrder(newTasksOrder, tasksWithProjectOrder);
        newTasksOrder.forEach(taskId -> tasksWithProjectOrder.stream()
                .filter(task -> task.getId().equals(taskId))
                .forEach(task -> task.setProjectOrder(newTasksOrder.indexOf(taskId)))
        );
        return tasksWithProjectOrder.stream()
                .sorted(Comparator.comparing(TaskEntity::getProjectOrder))
                .map(TaskEntity::getId)
                .collect(Collectors.toList());
    }

    private static void validateTaskOrder(List<UUID> tasksOrder, Set<TaskEntity> tasksWithOrder) {
        Set<UUID> existingTasksIds = tasksWithOrder.stream()
                .map(TaskEntity::getId)
                .collect(Collectors.toSet());
        Set<UUID> tasksOrderIds = new HashSet<>(tasksOrder);
        boolean allPreviousTasksPresent = tasksOrderIds.containsAll(existingTasksIds);
        boolean allTasksExists = existingTasksIds.containsAll(tasksOrderIds);
        boolean isNewOrderDistinct = tasksOrderIds.size() == tasksOrder.size();
        if (!allPreviousTasksPresent) {
            existingTasksIds.removeAll(tasksOrderIds);
            throw new ListOrderException("Missing task ids: " + existingTasksIds);
        }
        if (!allTasksExists) {
            tasksOrderIds.removeAll(existingTasksIds);
            throw new ListOrderException("Non-existent task ids: " + tasksOrderIds);
        }
        if (!isNewOrderDistinct) {
            throw new ListOrderException("Tasks ids are not distinct: " + getDuplicateIds(tasksOrder));
        }
    }

    @Transactional
    public List<UUID> reorderTasksForDay(String userId, LocalDate day, List<UUID> newTasksOrder) {
        log.info("Reordering tasks for user: {} and day: {} with new order: {}", userId, day, newTasksOrder);
        Set<TaskEntity> tasksWithDayOrder = tasksRepository.findAndLockTasksWithDayOrder(userId, day);
        validateTaskOrder(newTasksOrder, tasksWithDayOrder);
        newTasksOrder.forEach(taskId -> tasksWithDayOrder.stream()
                .filter(task -> task.getId().equals(taskId))
                .forEach(task -> task.setDayOrder(newTasksOrder.indexOf(taskId)))
        );
        return tasksWithDayOrder.stream()
                .sorted(Comparator.comparing(TaskEntity::getDayOrder))
                .map(TaskEntity::getId)
                .collect(Collectors.toList());
    }

    private static Set<String> getDuplicateIds(List<UUID> newTasksOrderIds) {
        return newTasksOrderIds.stream()
                .filter(id -> newTasksOrderIds.stream()
                        .filter(id::equals)
                        .count() > 1)
                .map(UUID::toString)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void setOrderForDayAndProject(String userId, TaskEntity taskEntity) {
        setProjectOrder(userId, taskEntity);
        if (taskEntity.getStartDay() != null && taskEntity.getStartTime() == null) {
            this.setDayOrder(userId, taskEntity);
        }
    }

    private void setProjectOrder(String userId, TaskEntity taskEntity) {
        setProjectOrder(userId, taskEntity, taskEntity.getProject());
    }
    private void setProjectOrder(String userId, TaskEntity taskEntity, ProjectEntity project) {
        int lastPosition = tasksRepository.findLastProjectOrder(userId, project)
                .orElse(-1);
        taskEntity.setProjectOrder(lastPosition + 1);
    }

    private void setDayOrder(String userId, TaskEntity taskEntity) {
        setDayOrder(userId, taskEntity, taskEntity.getStartDay());
    }

    private void setDayOrder(String userId, TaskEntity taskEntity, LocalDate day) {
        int lastPosition = tasksRepository.findLastDayOrder(userId, day)
                .orElse(-1);
        taskEntity.setDayOrder(lastPosition + 1);
    }

    @Transactional
    public void unsetOrderForDayAndProject(String userId, TaskEntity taskEntity) {
        this.unsetProjectOrder(userId, taskEntity);
        this.unsetDayOrder(userId, taskEntity);
    }

    private void unsetProjectOrder(String userId, TaskEntity taskEntity) {
        if(hasProjectOrder(taskEntity)) {
            tasksRepository.shiftProjectOrderOfAllTasksAfterDeletedOne(userId, taskEntity.getProject(),
                    taskEntity.getProjectOrder());
            taskEntity.setProjectOrder(null);
        }
    }

    @Transactional
    public void unsetDayOrder(String userId, TaskEntity taskEntity) {
        if (hasDayOrder(taskEntity)) {
            tasksRepository.shiftDayOrderOfAllTasksAfterDeletedOne(userId, taskEntity.getStartDay(),
                    taskEntity.getDayOrder());
            taskEntity.setDayOrder(null);
        }
    }

    /**
     * Updates day order for all tasks in the day.
     * Fixes day order to be continuous and without gaps.
     * Fills missing day order for tasks without start time.
     * @param userId
     * @param day
     */
    @Transactional
    public void updateDayOrder(String userId, LocalDate day) {
        List<TaskEntity> taskEntities = tasksRepository.findAndLockAllByUserIdAndStartDayAndStartTimeIsNull(userId,
                day);

        List<TaskEntity> entitiesWithDayOrder = taskEntities.stream()
                .filter(taskEntity -> taskEntity.getDayOrder() != null)
                .sorted(Comparator.comparing(TaskEntity::getDayOrder))
                .toList();
        for (int i = 0; i < entitiesWithDayOrder.size(); i++) {
            TaskEntity taskEntity = entitiesWithDayOrder.get(i);
            taskEntity.setDayOrder(i);
        }
        setMissingDayOrderForTasks(taskEntities);
    }

    private void setMissingDayOrderForTasks(List<TaskEntity> taskEntities) {
        Optional<Integer> maxDayOrder = taskEntities.stream()
                .map(TaskEntity::getDayOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo);
        AtomicInteger dayOrderCounter = new AtomicInteger(maxDayOrder.orElse(-1));

        taskEntities.stream()
                .filter(taskEntity -> taskEntity.getStartTime() == null && taskEntity.getDayOrder() == null)
                .forEach(taskEntity -> taskEntity.setDayOrder(dayOrderCounter.incrementAndGet()));
    }

    @Transactional
    public void updateProjectOrder(String userId, TaskEntity entity, ProjectEntity updateProject) {
        if (hasProjectOrder(entity)) {
            if (entity.getProject().equals(updateProject)) {
                return;
            }
            unsetProjectOrder(userId, entity);
        }
        setProjectOrder(userId, entity, updateProject);
    }


    @Transactional
    public void updateDayOrder(String userId, TaskEntity task, JsonNullable<LocalDate> updateStartDay,
                               JsonNullable<LocalTime> updateStartTime) {
        LocalDate startDay = task.getStartDay();
        LocalTime startTime = task.getStartTime();
        if (hasDayOrder(task)) {
            if (shouldUnsetDayOrder(updateStartDay, updateStartTime)) {
                this.unsetDayOrder(userId, task);
                return;
            }
            if (shouldSetDayOrderToDifferentDay(updateStartDay, startDay)) {
                this.unsetDayOrder(userId, task);
                this.setDayOrder(userId, task, updateStartDay.get());
                return;
            }
        }
        if (shouldSetDayOrderToUpdatedStartDay(updateStartDay, updateStartTime, startTime)) {
            this.setDayOrder(userId, task, updateStartDay.get());
            return;
        }
        if (shouldSetDayOrderToCurrentStartDay(updateStartDay, updateStartTime, startDay)) {
            this.setDayOrder(userId, task, startDay);
            return;
        }
    }

    private static boolean hasDayOrder(TaskEntity task) {
        return task.getDayOrder() != null;
    }

    private static boolean hasProjectOrder(TaskEntity task) {
        return task.getProjectOrder() != null;
    }

    private static boolean shouldSetDayOrderToDifferentDay(JsonNullable<LocalDate> updateStartDay, LocalDate startDay) {
        return updateStartDay != null && startDay != updateStartDay.get();
    }

    private static boolean shouldUnsetDayOrder(JsonNullable<LocalDate> updateStartDay,
                                               JsonNullable<LocalTime> updateStartTime) {
        return presentAndNull(updateStartDay) || presentAndNotNull(updateStartTime);
    }

    private static boolean shouldSetDayOrderToCurrentStartDay(JsonNullable<LocalDate> updateStartDay,
                                                              JsonNullable<LocalTime> updateStartTime,
                                                              LocalDate startDay) {
        return startDay != null && startDayNotChanged(updateStartDay, startDay) && presentAndNull(updateStartTime);
    }

    private static boolean startDayNotChanged(JsonNullable<LocalDate> updateStartDay, LocalDate startDay) {
        return updateStartDay == null || startDay.equals(updateStartDay.get());
    }

    private static boolean shouldSetDayOrderToUpdatedStartDay(JsonNullable<LocalDate> updateStartDay,
                                                              JsonNullable<LocalTime> updateStartTime,
                                                              LocalTime startTime) {
        return presentAndNotNull(updateStartDay) && (startTimeIsAlreadyNull(updateStartTime, startTime) || startTimeIsUpdatedToNull(updateStartTime, startTime));
    }

    private static boolean startTimeIsUpdatedToNull(JsonNullable<LocalTime> updateStartTime, LocalTime startTime) {
        return startTime != null && presentAndNull(updateStartTime);
    }

    private static boolean startTimeIsAlreadyNull(JsonNullable<LocalTime> updateStartTime, LocalTime startTime) {
        return startTime == null && !presentAndNotNull(updateStartTime);
    }

    private static boolean presentAndNull(JsonNullable<?> nullable) {
        return nullable != null && nullable.get() == null;
    }

    private static boolean presentAndNotNull(JsonNullable<?> nullable) {
        return nullable != null && nullable.get() != null;
    }
}

