package com.pw.timeplanner.feature.tasks;

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
        Set<Task> tasksWithDayOrder = tasksRepository.findAndLockTasksWithDayOrder(userId, day);
        return reorder(tasksWithDayOrder, newTasksOrder, Task::setDayOrder, Task::getDayOrder);
    }

    @Transactional
    @Override
    void setOrder(String userId, Task task) {
        if (task.getStartDay() != null && task.getStartTime() == null) {
            this.setOrder(userId, task, task.getStartDay());
        }
    }

    private void setOrder(String userId, Task task, LocalDate day) {
        int lastPosition = tasksRepository.findLastDayOrder(userId, day)
                .orElse(-1);
        task.setDayOrder(lastPosition + 1);
    }

    @Transactional
    @Override
    void unsetOrder(String userId, Task task) {
        if (hasDayOrder(task)) {
            tasksRepository.shiftDayOrderOfAllTasksAfterDeletedOne(userId, task.getStartDay(),
                    task.getDayOrder());
            task.setDayOrder(null);
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
    void updateOrder(String userId, LocalDate day) {
        List<Task> taskEntities = tasksRepository.findAndLockAllByUserIdAndStartDayAndStartTimeIsNull(userId,
                day);
        List<Task> entitiesWithDayOrder = taskEntities.stream()
                .filter(taskEntity -> taskEntity.getDayOrder() != null)
                .sorted(Comparator.comparing(Task::getDayOrder))
                .toList();
        for (int i = 0; i < entitiesWithDayOrder.size(); i++) {
            Task task = entitiesWithDayOrder.get(i);
            task.setDayOrder(i);
        }
        setMissingDayOrderForTasks(taskEntities);
    }

    private void setMissingDayOrderForTasks(List<Task> taskEntities) {
        Optional<Integer> maxDayOrder = taskEntities.stream()
                .map(Task::getDayOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo);
        AtomicInteger dayOrderCounter = new AtomicInteger(maxDayOrder.orElse(-1));

        taskEntities.stream()
                .filter(taskEntity -> taskEntity.getStartTime() == null && taskEntity.getDayOrder() == null)
                .forEach(taskEntity -> taskEntity.setDayOrder(dayOrderCounter.incrementAndGet()));
    }

    @Transactional
    void updateOrder(String userId, Task task, JsonNullable<LocalDate> updateStartDay,
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

    private static boolean hasDayOrder(Task task) {
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

