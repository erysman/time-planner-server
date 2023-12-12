package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.feature.tasks.TasksProperties;
import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskUpdateDTO;
import com.pw.timeplanner.feature.tasks.service.exceptions.NullDurationMinException;
import com.pw.timeplanner.feature.tasks.service.exceptions.TimeGranularityException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TasksValidator {
    private final TasksProperties properties;

    public void validate(CreateTaskDTO createTaskDTO) {
        if (createTaskDTO.getStartTime() != null
                && createTaskDTO.getDurationMin() == null) {
            throw new NullDurationMinException();
        }
        this.validateTimeGranularity(createTaskDTO);
    }

    public void validate(TaskUpdateDTO taskUpdateDTO) {
        if (taskUpdateDTO.getStartTime() != null && taskUpdateDTO.getStartTime().isPresent()
                && taskUpdateDTO.getDurationMin() == null) {
            throw new NullDurationMinException();
        }
        this.validateTimeGranularity(taskUpdateDTO);
    }

    private void validateTimeGranularity(CreateTaskDTO createTaskDTO) {
        Optional.of(createTaskDTO.getStartTime())
                .ifPresent(startTime -> this.validateTimeGranularity(startTime.getMinute(), "startTime"));
        validateTimeGranularity(createTaskDTO.getDurationMin(), "durationMin");
    }

    private void validateTimeGranularity(TaskUpdateDTO taskUpdateDTO) {
        taskUpdateDTO.getStartTime()
                .ifPresent(startTime -> this.validateTimeGranularity(startTime.getMinute(), "startTime"));
        validateTimeGranularity(taskUpdateDTO.getDurationMin(), "durationMin");
    }

    private void validateTimeGranularity(Integer minutes, String field) {
        if (minutes == null) {
            return;
        }
        Integer timeGranularityMinutes = properties.getTimeGranularityMinutes();
        int remainder = minutes % timeGranularityMinutes;
        if (remainder != 0) {
            throw new TimeGranularityException(field, timeGranularityMinutes);
        }
    }


}
