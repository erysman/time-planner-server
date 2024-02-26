package com.pw.timeplanner.feature.projects.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

@Value
@Builder
public class ProjectDTO implements Serializable {
    @NotNull
    UUID id;

    @NotBlank
    String name;

    @Schema(type = "String", pattern = "HH:mm", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime scheduleStartTime;

    @Schema(type = "String", pattern = "HH:mm", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime scheduleEndTime;

    @Schema(nullable = true)
    String color;
}
