package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.core.exception.ResourceNotFoundException;
import com.pw.timeplanner.feature.tasks.api.TasksResource;
import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.UpdateTaskDTO;
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntityMapper;
import com.pw.timeplanner.feature.tasks.repository.TasksRepository;
import com.pw.timeplanner.feature.tasks.service.validator.TasksValidator;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class TasksService {

    private final ProjectService projectService;
    private final TasksRepository tasksRepository;
    private final TaskEntityMapper mapper;
    private final TasksOrderService tasksOrderService;
    private final TasksValidator tasksValidator;
    private final TasksProperties properties;

    @Transactional
    public TaskDTO createTask(String userId, CreateTaskDTO createTaskDTO) {
        tasksValidator.validate(createTaskDTO);
        ProjectEntity projectEntity = getProject(userId, createTaskDTO);
        TaskEntity entity = mapper.createEntity(createTaskDTO);
        entity.setUserId(userId);
        entity.setProject(projectEntity);
        tasksOrderService.setOrderForDayAndProject(userId, entity);
        TaskEntity saved = tasksRepository.save(entity);
        return mapper.toDTO(saved);
    }

    private ProjectEntity getProject(String userId, CreateTaskDTO createTaskDTO) {
        if (createTaskDTO.getProjectId() == null) {
            return projectService.getOrCreateDefaultProjectEntity(userId);
        } else {
            return projectService.getProjectEntity(userId, createTaskDTO.getProjectId());
        }
    }

    public List<TaskDTO> getTasksByDate(String userId, LocalDate startDate) {
        return tasksRepository.findAllByUserIdAndStartDay(userId, startDate)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    public TaskDTO getTask(String userId, UUID taskId) {
        return mapper.toDTO(tasksRepository.findOneByUserIdAndId(userId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException(TasksResource.RESOURCE_PATH, taskId)));
    }

    @Transactional
    public void deleteTask(String userId, UUID taskId) {
        TaskEntity entity = tasksRepository.findOneByUserIdAndId(userId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException(TasksResource.RESOURCE_PATH, taskId));
        tasksOrderService.unsetOrderForDayAndProject(userId, entity);
        tasksRepository.delete(entity);
    }

    @Transactional
    public TaskDTO updateTask(String userId, UUID taskId, UpdateTaskDTO updateTaskDTO) {
        log.info("Updating task " + taskId + " with: " + updateTaskDTO);
        TaskEntity entity = tasksRepository.findAndLockOneByUserIdAndId(userId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException(TasksResource.RESOURCE_PATH, taskId));
        tasksValidator.validate(updateTaskDTO);
        if (updateTaskDTO.getStartTime() != null || updateTaskDTO.getStartDay() != null) {
            tasksOrderService.updateDayOrder(userId, entity, updateTaskDTO.getStartDay(), updateTaskDTO.getStartTime());
            entity.setAutoScheduled(false);
        }
        if (updateTaskDTO.getProjectId() != null) {
            entity.setProject(projectService.getProjectEntity(userId, updateTaskDTO.getProjectId()));
        }
        mapper.update(updateTaskDTO, entity);
        return mapper.toDTO(entity);
    }

}
