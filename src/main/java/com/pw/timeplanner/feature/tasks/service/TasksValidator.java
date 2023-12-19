package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.feature.tasks.TasksProperties;
import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskUpdateDTO;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.service.exceptions.NullDurationMinException;
import com.pw.timeplanner.feature.tasks.service.exceptions.TimeGranularityException;
import lombok.AllArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TasksValidator {
    private final TasksProperties properties;

    public void validate(CreateTaskDTO createTaskDTO) {
        if (createTaskDTO.getStartTime() != null
                && createTaskDTO.getDurationMin() == null) {
            throw new NullDurationMinException();
        }
//        this.validateTimeGranularity(createTaskDTO);
    }

    public void validateUpdate(TaskUpdateDTO taskUpdateDTO, TaskEntity existingEntity) {
        /*TODO
            throw if:
                updateStartTime is present and not null
                    and updateDuration is present and null or updateDuration is not present and existingDuration is null
                updateStartTime is not present and existingStartTime is not null
                    and updateDuration is present and null
         */
        boolean isStartTimeUpdatedToNonNull = isPresentAndNotNull(taskUpdateDTO.getStartTime())
                && (isPresentAndNull(taskUpdateDTO.getDurationMin())
                    || taskUpdateDTO.getDurationMin() == null
                    && existingEntity.getDurationMin() == null);
        boolean isStartTimeNotUpdatedToNull = taskUpdateDTO.getStartTime() == null
                && existingEntity.getStartTime() != null
                && isPresentAndNull(taskUpdateDTO.getDurationMin());
        if (isStartTimeUpdatedToNonNull
                || isStartTimeNotUpdatedToNull
        ) {
            throw new NullDurationMinException();
        }
//        this.validateTimeGranularity(taskUpdateDTO);
    }

    private static boolean isPresentAndNull(JsonNullable<?> nullable) {
        return nullable != null && nullable.get() == null;
    }

    private static boolean isPresentAndNotNull(JsonNullable<?> nullable) {
        return nullable != null && nullable.get() != null;
    }


    private void validateTimeGranularity(CreateTaskDTO createTaskDTO) {
        if (createTaskDTO.getStartTime() != null) {
            validateTimeGranularity(createTaskDTO.getStartTime().getMinute(), "startTime");
        }
        validateTimeGranularity(createTaskDTO.getDurationMin(), "durationMin");
    }

    private void validateTimeGranularity(TaskUpdateDTO taskUpdateDTO) {
        if (isPresentAndNotNull(taskUpdateDTO.getStartTime())) {
            validateTimeGranularity(taskUpdateDTO.getStartTime().get().getMinute(), "startTime");
        }
        if (isPresentAndNotNull(taskUpdateDTO.getDurationMin())) {
            validateTimeGranularity(taskUpdateDTO.getDurationMin().get(), "durationMin");
        }
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
