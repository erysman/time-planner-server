package com.pw.timeplanner.feature.tasks.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.core.validation.DurationMinValid;
import com.pw.timeplanner.core.validation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskDTO implements Serializable {
    @NullOrNotBlank
    @Size(min = 1, max = 150)
    String name;

    @Schema(type = "String", pattern = "yyyy-MM-dd", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @FutureOrPresent
    JsonNullable<LocalDate> startDay;

    @Schema(type = "String", pattern = "HH:mm", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    JsonNullable<LocalTime> startTime;

    @Schema(type = "integer", nullable = true)
    @DurationMinValid(tasksProperties = TasksProperties.class)
    JsonNullable<Integer> durationMin;

    @Schema(nullable = true)
    Boolean isImportant;
    @Schema(nullable = true)
    Boolean isUrgent;

    @Schema(nullable = true)
    UUID projectId;
}
