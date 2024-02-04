package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.repository.TasksRepository;
import com.pw.timeplanner.feature.tasks.service.exceptions.DataConflictException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
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

//    @Transactional
//    public List<UUID> reorderTasksForDay(String userId, LocalDate day, List<UUID> tasksOrder) throws DataConflictException {
//        Set<UUID> previousTaskOrder = tasksRepository.findAndLockTaskIdsWithDayOrder(userId, day);
//        Set<UUID> tasksOrderSet = new HashSet<>(tasksOrder);
//        boolean allTasksPresent = tasksOrderSet.containsAll(previousTaskOrder);
//        if(!allTasksPresent) {
//            previousTaskOrder.removeAll(tasksOrderSet);
//            throw new DataConflictException("Missing task ids: " + previousTaskOrder);
//        }
//        int totalUpdatedRows = 0;
//        for (int i = 0; i < tasksOrder.size(); i++) {
//            int updatedRows = tasksRepository.updateDayOrder(userId, tasksOrder.get(i), i);
//            totalUpdatedRows += updatedRows;
//        }
//        List<UUID> newTaskOrder = this.getTasksOrderForDay(userId, day);
//        log.info("updated rows: "+totalUpdatedRows + " previous tasks count: "+tasksOrder.size() + " new task count: "+newTaskOrder.size());
//        return newTaskOrder;
//    }

    @Transactional
    public List<UUID> reorderTasksForDay(String userId, LocalDate day, List<UUID> tasksOrder) throws DataConflictException {
        Set<TaskEntity> previousTaskOrder = tasksRepository.findAndLockTasksWithDayOrder(userId, day);
        Set<UUID> uuidList = previousTaskOrder.stream()
                .map(TaskEntity::getId)
                .collect(Collectors.toSet());
        Set<UUID> tasksOrderSet = new HashSet<>(tasksOrder);
        boolean allTasksPresent = tasksOrderSet.containsAll(uuidList);
        if(!allTasksPresent) {
            uuidList.removeAll(tasksOrderSet);
            throw new DataConflictException("Missing task ids: " + uuidList);
        }
        tasksOrder.forEach(taskId -> {
            previousTaskOrder.stream()
                    .filter(task -> task.getId()
                            .equals(taskId))
                    .forEach(task -> task.setDayOrder(tasksOrder.indexOf(taskId)));
        });
        List<UUID> newTaskOrder = this.getTasksOrderForDay(userId, day);
        return newTaskOrder;
    }

    @Transactional
    public void setOrderForDayAndProject(String userId, TaskEntity taskEntity) {
        /*
            if startDate exist, but startTime not, then set dayListPosition to last+1
            TODO: if project exist, then set projectListPosition to last+1
         */
        if (taskEntity.getStartDay() != null && taskEntity.getStartTime() == null) {
            this.setDayOrder(userId, taskEntity, taskEntity.getStartDay());
        }
    }

    @Transactional
    public void setDayOrder(String userId, TaskEntity taskEntity, LocalDate day) {
        int lastPosition = tasksRepository.findLastDayOrder(userId, day).orElse(0);
        taskEntity.setDayOrder(lastPosition + 1);
    }

    @Transactional
    public void unsetOrderForDayAndProject(String userId, TaskEntity taskEntity) {
        this.unsetDayOrder(userId, taskEntity);
    }

    @Transactional
    public void unsetDayOrder(String userId, TaskEntity taskEntity) {
        /*
            if taskEntity has dayListPosition:
                update all taskEntities with higher dayposition to their position -1.
            TODO: if taskEntity has projectListPosition
                update all taskEntities with higher dayposition to their position -1.
         */
        if (hasDayOrder(taskEntity)) {
            tasksRepository.shiftOrderOfAllTasksAfterDeletedOne(userId, taskEntity.getStartDay(), taskEntity.getDayOrder());
            taskEntity.setDayOrder(null);
        }
    }

    //update day order for all tasks in the day
    @Transactional
    public void updateDayOrder(String userId, LocalDate day) {
        List<TaskEntity> taskEntities = tasksRepository.findTasksWithDayOrderOrderedByDayOrder(userId, day);
        for (int i = 0; i < taskEntities.size(); i++) {
            TaskEntity taskEntity = taskEntities.get(i);
            taskEntity.setDayOrder(i);
        }
    }

    @Transactional
    public void setMissingDayOrder(List<TaskEntity> taskEntities) {
        Optional<Integer> maxDayOrder = taskEntities.stream()
                .map(TaskEntity::getDayOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo);
        AtomicInteger dayOrderCounter = new AtomicInteger(maxDayOrder.orElse(0));

        taskEntities.stream()
                .filter(taskEntity -> taskEntity.getStartTime() == null && taskEntity.getDayOrder() == null)
                .forEach(taskEntity -> {
                    taskEntity.setDayOrder(dayOrderCounter.incrementAndGet());
                });
    }

    @Transactional
    public void updateDayOrder(String userId, TaskEntity task, JsonNullable<LocalDate> updateStartDay, JsonNullable<LocalTime> updateStartTime) {
        if (hasDayOrder(task)) {
            if (shouldUnsetDayOrder(updateStartDay, updateStartTime)) {
                this.unsetDayOrder(userId, task);
                return;
            }
            if (shouldSetDayOrderToDifferentDay(task, updateStartDay)) {
                this.unsetDayOrder(userId, task);
                this.setDayOrder(userId, task, updateStartDay.get());
                return;
            }
        }
        if (shouldSetDayOrderToUpdatedStartDay(task, updateStartDay, updateStartTime)) {
            this.setDayOrder(userId, task, updateStartDay.get());
            return;
        }
        if (shouldSetDayOrderToCurrentStartDay(task, updateStartDay, updateStartTime)) {
            this.setDayOrder(userId, task, task.getStartDay());
            return;
        }
    }

    private static boolean hasDayOrder(TaskEntity task) {
        return task.getDayOrder() != null;
    }

    private static boolean shouldSetDayOrderToDifferentDay(TaskEntity task, JsonNullable<LocalDate> updateStartDay) {
        return updateStartDay != null && task.getStartDay() != updateStartDay.get();
    }

    private static boolean shouldUnsetDayOrder(JsonNullable<LocalDate> updateStartDay, JsonNullable<LocalTime> updateStartTime) {
        return (updateStartDay != null && updateStartDay.get() == null)
                || (updateStartTime != null && updateStartTime.get() != null);
    }

    private static boolean shouldSetDayOrderToCurrentStartDay(TaskEntity task, JsonNullable<LocalDate> updateStartDay, JsonNullable<LocalTime> updateStartTime) {
        return task.getStartDay() != null && task.getStartTime() != null
                && (updateStartDay == null || updateStartDay.get() == null)
                && updateStartTime != null && updateStartTime.get() == null;
    }

    private static boolean shouldSetDayOrderToUpdatedStartDay(TaskEntity task, JsonNullable<LocalDate> updateStartDay, JsonNullable<LocalTime> updateStartTime) {
        return task.getStartDay() == null && task.getStartTime() == null
                && updateStartDay != null && updateStartDay.get() != null
                && updateStartTime == null;
    }
}

