package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.api.dto.TaskDTO;
import com.pw.timeplanner.api.dto.TaskUpdateDTO;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntityMapper;
import com.pw.timeplanner.feature.tasks.repository.TasksRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class TasksService {

    private final TasksRepository tasksRepository;
    private final TaskEntityMapper mapper;

    public List<TaskDTO> getTasks(String userId, LocalDate startDate) {
        return tasksRepository.findAllByUserIdAndStartDate(userId, startDate).stream().map(mapper::toDTO).toList();
    }

    public Optional<TaskDTO> getTask(String userId, UUID taskId) {
        return tasksRepository.findOneByUserIdAndId(userId, taskId).map(mapper::toDTO);
    }

    public Optional<TaskDTO> updateTask(String userId, UUID taskId, TaskUpdateDTO taskUpdateDTO) {
        Optional<TaskEntity> entity = tasksRepository.findOneByUserIdAndId(userId, taskId);
        if(entity.isEmpty()) {
            return Optional.empty();
        }
        TaskEntity task = entity.get();
        /* TODO: validation for different fields (during creation and update of specific fields)
            * durationMin
                * can't be negative
            * startDate ?
            * startTime ?
            * name ?
         */
        TaskEntity updatedTask = mapper.updateEntityFromDTO(taskUpdateDTO, task);
        return Optional.of(mapper.toDTO(tasksRepository.save(updatedTask)));
    }

    @PostConstruct
    private void initData() {
        String userId = "oNK797T3SAfA0Z4nvy8oFWR7WOi2";

        if(!tasksRepository.findAllByUserId(userId).isEmpty()) {
            return;
        }
        log.info("Initializing db with mocked entities");
        TaskEntity e1 = TaskEntity.builder().userId(userId).name("Sprzątanie").startDate(LocalDate.now()).build();
        TaskEntity e2 = TaskEntity.builder().userId(userId).name("Gotowanie").startDate(LocalDate.now()).startTime(LocalTime.of(9, 0, 0)).durationMin(60).build();
        TaskEntity e3 = TaskEntity.builder().userId(userId).name("Gotowanie2").startDate(LocalDate.now()).startTime(LocalTime.of(10, 15, 0)).durationMin(120).build();
        tasksRepository.save(e1);
        tasksRepository.save(e2);
        tasksRepository.save(e3);
    }

}
