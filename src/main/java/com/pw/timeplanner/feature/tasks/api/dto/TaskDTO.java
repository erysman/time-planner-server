package com.pw.timeplanner.feature.tasks.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Value
@Builder
public class TaskDTO implements Serializable {
    @NotNull
    UUID id;

    @NotBlank
    String name;

    @Schema(type = "String", pattern = "yyyy-MM-dd", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate startDay;

    @Schema(type = "String", pattern = "HH:mm", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime startTime;

    @Schema(nullable = true)
    Integer durationMin;

    @NotNull
    Boolean isImportant;
    @NotNull
    Boolean isUrgent;
    @NotNull
    UUID projectId;
}
