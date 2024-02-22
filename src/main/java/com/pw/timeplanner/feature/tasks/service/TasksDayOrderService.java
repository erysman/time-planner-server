package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.repository.TasksRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@AllArgsConstructor
@Slf4j
public class TasksDayOrderService extends TasksOrderService<LocalDate> {

    private final TasksRepository tasksRepository;

    @Override
    public List<UUID> getOrder(String userId, LocalDate day) {
        log.info("Getting tasks order for user: {} and day: {}", userId, day);
        return tasksRepository.findTaskIdsOrderedByDayOrder(userId, day);
    }

    @Transactional
    @Override
    public List<UUID> reorder(String userId, LocalDate day, List<UUID> newTasksOrder) {
        log.info("Reordering tasks for user: {} and day: {} with new order: {}", userId, day, newTasksOrder);
        Set<TaskEntity> tasksWithDayOrder = tasksRepository.findAndLockTasksWithDayOrder(userId, day);
        return reorder(tasksWithDayOrder, newTasksOrder, TaskEntity::setDayOrder, TaskEntity::getDayOrder);
    }

    @Transactional
    @Override
    public void setOrder(String userId, TaskEntity taskEntity) {
        if (taskEntity.getStartDay() != null && taskEntity.getStartTime() == null) {
            this.setOrder(userId, taskEntity, taskEntity.getStartDay());
        }
    }

    private void setOrder(String userId, TaskEntity taskEntity, LocalDate day) {
        int lastPosition = tasksRepository.findLastDayOrder(userId, day)
                .orElse(-1);
        taskEntity.setDayOrder(lastPosition + 1);
    }

    @Transactional
    @Override
    public void unsetOrder(String userId, TaskEntity taskEntity) {
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
     *
     * @param userId
     * @param day
     */
    @Transactional
    public void updateOrder(String userId, LocalDate day) {
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
    public void updateOrder(String userId, TaskEntity task, JsonNullable<LocalDate> updateStartDay,
                            JsonNullable<LocalTime> updateStartTime) {
        LocalDate startDay = task.getStartDay();
        LocalTime startTime = task.getStartTime();
        if (hasDayOrder(task)) {
            if (shouldUnsetDayOrder(updateStartDay, updateStartTime)) {
                this.unsetOrder(userId, task);
                return;
            }
            if (shouldSetDayOrderToDifferentDay(updateStartDay, startDay)) {
                this.unsetOrder(userId, task);
                this.setOrder(userId, task, updateStartDay.get());
                return;
            }
        }
        if (shouldSetDayOrderToUpdatedStartDay(updateStartDay, updateStartTime, startTime)) {
            this.setOrder(userId, task, updateStartDay.get());
            return;
        }
        if (shouldSetDayOrderToCurrentStartDay(updateStartDay, updateStartTime, startDay)) {
            this.setOrder(userId, task, startDay);
            return;
        }
    }

    private static boolean hasDayOrder(TaskEntity task) {
        return task.getDayOrder() != null;
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

