package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.UpdateTaskDTO;
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntityMapper;
import com.pw.timeplanner.feature.tasks.repository.ProjectsRepository;
import com.pw.timeplanner.feature.tasks.repository.TasksRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class TasksService {

    private final ProjectsRepository projectsRepository;
    private final TasksRepository tasksRepository;
    private final TaskEntityMapper mapper;
    private final TasksOrderService tasksOrderService;
    private final TasksValidator tasksValidator;
    private final TasksProperties properties;

    @Transactional
    public Optional<TaskDTO> createTask(String userId, CreateTaskDTO createTaskDTO) {
        tasksValidator.validate(createTaskDTO);
        ProjectEntity projectEntity = getProject(userId, createTaskDTO);
        TaskEntity entity = mapper.createEntity(createTaskDTO);
        entity.setUserId(userId);
        entity.setProject(projectEntity);
        tasksOrderService.setOrderForDayAndProject(userId, entity);
        TaskEntity saved = tasksRepository.save(entity);
        return Optional.of(mapper.toDTO(saved));
    }

    private ProjectEntity getProject(String userId, CreateTaskDTO createTaskDTO) {
        if (createTaskDTO.getProjectId() == null) {
            return getDefaultProject(userId);
        } else {
            return getProjectById(userId, createTaskDTO.getProjectId());
        }
    }

    private ProjectEntity getDefaultProject(String userId) {
        Optional<ProjectEntity> defaultProjectEntity = projectsRepository.findOneByUserIdAndName(userId,
                properties.getDefaultProjectName());
        return defaultProjectEntity.orElseThrow();
    }

    private ProjectEntity getProjectById(String userId, UUID projectId) {
        Optional<ProjectEntity> projectEntity = projectsRepository.findOneByUserIdAndId(userId, projectId);
        return projectEntity.orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
    }

    public List<TaskDTO> getTasksByDate(String userId, LocalDate startDate) {
        return tasksRepository.findAllByUserIdAndStartDay(userId, startDate)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    public Optional<TaskDTO> getTask(String userId, UUID taskId) {
        return tasksRepository.findOneByUserIdAndId(userId, taskId)
                .map(mapper::toDTO);
    }

    @Transactional
    public boolean deleteTask(String userId, UUID taskId) {
        Optional<TaskEntity> entity = tasksRepository.findOneByUserIdAndId(userId, taskId);
        if (entity.isEmpty()) {
            return false;
        }
        tasksOrderService.unsetOrderForDayAndProject(userId, entity.get());
        tasksRepository.delete(entity.get());
        return true;
    }

    @Transactional
    public Optional<TaskDTO> updateTask(String userId, UUID taskId, UpdateTaskDTO updateTaskDTO) {
        log.info("Updating task " + taskId + " with: " + updateTaskDTO);
        Optional<TaskEntity> entity = tasksRepository.findAndLockOneByUserIdAndId(userId, taskId);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        TaskEntity task = entity.get();
        tasksValidator.validate(updateTaskDTO);
        if (updateTaskDTO.getStartTime() != null || updateTaskDTO.getStartDay() != null) {
            tasksOrderService.updateDayOrder(userId, task, updateTaskDTO.getStartDay(), updateTaskDTO.getStartTime());
            task.setAutoScheduled(false);
        }
        if(updateTaskDTO.getProjectId() != null) {
            Optional<ProjectEntity> projectEntity = projectsRepository.findOneByUserIdAndId(userId, updateTaskDTO.getProjectId());
            projectEntity.ifPresent(task::setProject);
        }
        mapper.update(updateTaskDTO, task);
        return Optional.of(mapper.toDTO(task));
    }

}
