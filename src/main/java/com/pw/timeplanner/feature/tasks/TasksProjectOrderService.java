package com.pw.timeplanner.feature.tasks;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class TasksProjectOrderService extends TasksOrderService<UUID> {

    private final TasksRepository tasksRepository;

    @Override
    public List<UUID> getOrder(String userId, UUID projectId) {
        log.info("Getting tasks order for user: {} and project: {}", userId, projectId);
        return tasksRepository.findTaskIdsOrderedByProject(userId, projectId);
    }

    @Transactional
    @Override
    public List<UUID> reorder(String userId, UUID projectId, List<UUID> newTasksOrder) {
        log.info("Reordering tasks for user: {} and project: {} with new order: {}", userId, projectId, newTasksOrder);
        Set<Task> tasksWithProjectOrder = tasksRepository.findAndLockTasksWithProjectOrder(userId, projectId);
        return reorder(tasksWithProjectOrder, newTasksOrder, Task::setProjectOrder, Task::getProjectOrder);
    }

    @Transactional
    @Override
    void setOrder(String userId, Task task) {
        setOrder(userId, task, task.getProjectId());
    }
    private void setOrder(String userId, Task task, UUID projectId) {
        int lastPosition = tasksRepository.findLastProjectOrder(userId, projectId)
                .orElse(-1);
        task.setProjectOrder(lastPosition + 1);
    }

    @Transactional
    @Override
    void unsetOrder(String userId, Task task) {
        if(hasProjectOrder(task)) {
            tasksRepository.shiftProjectOrderOfAllTasksAfterDeletedOne(userId, task.getProjectId(),
                    task.getProjectOrder());
            task.setProjectOrder(null);
        }
    }

    @Transactional
    void updateOrder(String userId, Task entity, UUID projectId) {
        if (hasProjectOrder(entity)) {
            if (entity.getProjectId().equals(projectId)) {
                return;
            }
            unsetOrder(userId, entity);
        }
        setOrder(userId, entity, projectId);
    }
    private static boolean hasProjectOrder(Task task) {
        return task.getProjectOrder() != null;
    }
}

