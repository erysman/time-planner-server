package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.repository.TasksRepository;
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
public class TasksProjectOrderService extends TasksOrderService<ProjectEntity> {

    private final TasksRepository tasksRepository;

    @Override
    public List<UUID> getOrder(String userId, ProjectEntity project) {
        log.info("Getting tasks order for user: {} and project: {}", userId, project.getId());
        return tasksRepository.findTaskIdsOrderedByProject(userId, project);
    }

    @Transactional
    @Override
    public List<UUID> reorder(String userId, ProjectEntity project, List<UUID> newTasksOrder) {
        log.info("Reordering tasks for user: {} and project: {} with new order: {}", userId, project.getId(), newTasksOrder);
        Set<TaskEntity> tasksWithProjectOrder = tasksRepository.findAndLockTasksWithProjectOrder(userId, project);
        return reorder(tasksWithProjectOrder, newTasksOrder, TaskEntity::setProjectOrder, TaskEntity::getProjectOrder);
    }

    @Transactional
    @Override
    public void setOrder(String userId, TaskEntity taskEntity) {
        setOrder(userId, taskEntity, taskEntity.getProject());
    }
    private void setOrder(String userId, TaskEntity taskEntity, ProjectEntity project) {
        int lastPosition = tasksRepository.findLastProjectOrder(userId, project)
                .orElse(-1);
        taskEntity.setProjectOrder(lastPosition + 1);
    }

    @Transactional
    @Override
    public void unsetOrder(String userId, TaskEntity taskEntity) {
        if(hasProjectOrder(taskEntity)) {
            tasksRepository.shiftProjectOrderOfAllTasksAfterDeletedOne(userId, taskEntity.getProject(),
                    taskEntity.getProjectOrder());
            taskEntity.setProjectOrder(null);
        }
    }

    @Transactional
    public void updateOrder(String userId, TaskEntity entity, ProjectEntity updateProject) {
        if (hasProjectOrder(entity)) {
            if (entity.getProject().equals(updateProject)) {
                return;
            }
            unsetOrder(userId, entity);
        }
        setOrder(userId, entity, updateProject);
    }
    private static boolean hasProjectOrder(TaskEntity task) {
        return task.getProjectOrder() != null;
    }
}

