package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskUpdateDTO;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntityMapper;
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
@Transactional
public class TasksService {

    private final TasksRepository tasksRepository;
    private final TaskEntityMapper mapper;
    private final TasksOrderService tasksOrderService;
    private final TasksValidator tasksValidator;


    public Optional<TaskDTO> createTask(String userId, CreateTaskDTO createTaskDTO) {
        tasksValidator.validate(createTaskDTO);
        TaskEntity entity = mapper.createEntity(createTaskDTO);
        entity.setUserId(userId);
        tasksOrderService.setOrderForDayAndProject(userId, entity);
        TaskEntity saved = tasksRepository.save(entity);
        return Optional.of(mapper.toDTO(saved));
    }


    public List<TaskDTO> getTasks(String userId, LocalDate startDate) {
        return tasksRepository.findAllByUserIdAndStartDay(userId, startDate).stream().map(mapper::toDTO).toList();
    }

    public Optional<TaskDTO> getTask(String userId, UUID taskId) {
        return tasksRepository.findOneByUserIdAndId(userId, taskId).map(mapper::toDTO);
    }

    public boolean deleteTask(String userId, UUID taskId) {
        Optional<TaskEntity> entity = tasksRepository.findOneByUserIdAndId(userId, taskId);
        if (entity.isEmpty()) {
            return false;
        }
        tasksOrderService.unsetOrderForDayAndProject(userId, entity.get());
        tasksRepository.delete(entity.get());
        return true;
    }

    public Optional<TaskDTO> updateTask(String userId, UUID taskId, TaskUpdateDTO taskUpdateDTO) {
        tasksValidator.validate(taskUpdateDTO);
        Optional<TaskEntity> entity = tasksRepository.findOneByUserIdAndId(userId, taskId);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        TaskEntity task = entity.get();
        if (taskUpdateDTO.getStartTime() != null || taskUpdateDTO.getStartDay() != null) {
            tasksOrderService.updateDayOrder(userId, task, taskUpdateDTO.getStartDay(), taskUpdateDTO.getStartTime());
        }
        mapper.update(taskUpdateDTO, task);
//        TaskEntity updatedTask = this.updateEntityFromDTO(taskUpdateDTO, task);
        return Optional.of(mapper.toDTO(task));
    }

}
