package com.pw.timeplanner.feature.tasks.api.projectDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

@Value
@Builder
public class ProjectDTO implements Serializable {
    UUID id;

    @NotBlank
    String name;

    @Schema(type = "String", pattern = "HH:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime scheduleStartTime;

    @Schema(type = "String", pattern = "HH:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime scheduleEndTime;

    @Schema(nullable = true)
    String color;
}
