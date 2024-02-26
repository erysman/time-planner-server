package com.pw.timeplanner.feature.tasks;

import com.pw.timeplanner.feature.tasks.exceptions.ListOrderException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
abstract class TasksOrderService<T> {

    abstract List<UUID> getOrder(String userId, T param);

    abstract List<UUID> reorder(String userId, T param, List<UUID> newTasksOrder);

    @Transactional
    protected List<UUID> reorder(Set<Task> tasksWithOrder, List<UUID> newTasksOrder, BiConsumer<Task,
            Integer> setOrder, Function<Task, Integer> getOrder) {
        validateOrder(newTasksOrder, tasksWithOrder);
        newTasksOrder.forEach(taskId -> tasksWithOrder.stream()
                .filter(task -> task.getId()
                        .equals(taskId))
                .forEach(task -> setOrder.accept(task, newTasksOrder.indexOf(taskId))));
        return tasksWithOrder.stream()
                .sorted(Comparator.comparing(getOrder))
                .map(Task::getId)
                .collect(Collectors.toList());
    }

    private static void validateOrder(List<UUID> tasksOrder, Set<Task> tasksWithOrder) {
        Set<UUID> existingTasksIds = tasksWithOrder.stream()
                .map(Task::getId)
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

    private static Set<String> getDuplicateIds(List<UUID> newTasksOrderIds) {
        return newTasksOrderIds.stream()
                .filter(id -> newTasksOrderIds.stream()
                        .filter(id::equals)
                        .count() > 1)
                .map(UUID::toString)
                .collect(Collectors.toSet());
    }

    abstract void setOrder(String userId, Task task);

    abstract void unsetOrder(String userId, Task task);
}

