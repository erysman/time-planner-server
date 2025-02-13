package com.pw.timeplanner.feature.tasks.service.validator;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.feature.tasks.api.projectDto.CreateProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.UpdateProjectDTO;
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.service.exceptions.TimeGranularityException;
import com.pw.timeplanner.feature.tasks.service.exceptions.TimePeriodException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;

@Service
@AllArgsConstructor
public class ProjectsValidator {

    TasksProperties properties;

    public void validate(CreateProjectDTO createProjectDTO) {
        LocalTime startTime = createProjectDTO.getScheduleStartTime();
        LocalTime endTime = createProjectDTO.getScheduleEndTime();
        validateTimeGranularity(startTime, "scheduleStartTime");
        validateTimeGranularity(endTime, "scheduleEndTime");
        validateTimePeriod(startTime, endTime, "scheduleStartTime", false);
    }

    public void validate(UpdateProjectDTO updateProjectDTO, ProjectEntity projectEntity) {
        LocalTime updateStartTime = updateProjectDTO.getScheduleStartTime();
        LocalTime updateEndTime = updateProjectDTO.getScheduleEndTime();
        validateTimeGranularity(updateStartTime, "scheduleStartTime");
        validateTimeGranularity(updateEndTime, "scheduleEndTime");
        LocalTime startTime = updateStartTime == null ? projectEntity.getScheduleStartTime() : updateStartTime;
        LocalTime endTime = updateEndTime == null ? projectEntity.getScheduleEndTime() : updateEndTime;
        validateTimePeriod(startTime, endTime, "scheduleStartTime", true);
    }

    private static void validateTimePeriod(LocalTime startTime, LocalTime endTime, String field, boolean throwOnNulls) {
        if(startTime == null || endTime == null) {
            if(throwOnNulls) {
                throw new TimePeriodException(field);
            }
            return;
        }
        Duration duration = Duration.between(startTime, endTime);
        if (duration.isNegative() || duration.isZero()) {
            throw new TimePeriodException(field);
        }
    }

    private void validateTimeGranularity(LocalTime time, String field) {
        if (time == null) {
            return;
        }
        int minutes = time.getMinute();
        Integer timeGranularityMinutes = properties.getTimeGranularityMinutes();
        int remainder = minutes % timeGranularityMinutes;
        if (remainder != 0) {
            throw new TimeGranularityException(field, timeGranularityMinutes);
        }
    }

}
