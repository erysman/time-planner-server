package com.pw.timeplanner.feature.tasks;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.core.exception.ResourceNotFoundException;
import com.pw.timeplanner.feature.projects.ProjectService;
import com.pw.timeplanner.feature.tasks.api.TasksResource;
import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.UpdateTaskDTO;
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
    private final TasksDayOrderService tasksDayOrderService;
    private final TasksProjectOrderService tasksProjectOrderService;
    private final TasksValidator tasksValidator;
    private final TasksProperties properties;

    @Transactional
    public TaskDTO createTask(String userId, CreateTaskDTO dto) {
        tasksValidator.validate(dto);
        UUID projectId = getProjectId(userId, dto);
        Task entity = Task.builder()
                .userId(userId)
                .name(dto.getName())
                .projectId(projectId)
                .startDay(dto.getStartDay())
                .startTime(dto.getStartTime())
                .durationMin(dto.getDurationMin())
                .isImportant(dto.getIsImportant())
                .isUrgent(dto.getIsUrgent())
                .build();
        tasksDayOrderService.setOrder(userId, entity);
        tasksProjectOrderService.setOrder(userId, entity);
        Task saved = tasksRepository.save(entity);
        return mapper.toDTO(saved);
    }

    private UUID getProjectId(String userId, CreateTaskDTO createTaskDTO) {
        if (createTaskDTO.getProjectId() == null) {
            return projectService.getOrCreateDefaultProject(userId).getId();
        }
        return projectService.getProject(userId, createTaskDTO.getProjectId()).getId();
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
        Task entity = tasksRepository.findOneByUserIdAndId(userId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException(TasksResource.RESOURCE_PATH, taskId));
        tasksDayOrderService.unsetOrder(userId, entity);
        tasksProjectOrderService.unsetOrder(userId, entity);
        tasksRepository.delete(entity);
    }

    @Transactional
    public TaskDTO updateTask(String userId, UUID taskId, UpdateTaskDTO dto) {
        log.info("Updating task " + taskId + " with: " + dto);
        Task entity = tasksRepository.findAndLockOneByUserIdAndId(userId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException(TasksResource.RESOURCE_PATH, taskId));
        tasksValidator.validate(dto);
        if (dto.getStartTime() != null || dto.getStartDay() != null) {
            tasksDayOrderService.updateOrder(userId, entity, dto.getStartDay(), dto.getStartTime());
            entity.setAutoScheduled(false);
        }
        if (dto.getProjectId() != null) {
            tasksProjectOrderService.updateOrder(userId, entity, dto.getProjectId());
            entity.setProjectId(dto.getProjectId());
        }
        mapper.update(dto, entity);
        return mapper.toDTO(entity);
    }

    public List<TaskDTO> getProjectTasks(final String userId, final UUID projectId) {
        return tasksRepository.findAllByUserIdAndProjectId(userId, projectId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }
}
