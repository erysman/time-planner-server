package com.pw.timeplanner.feature.projects.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pw.timeplanner.core.validation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectDTO implements Serializable {

    @NullOrNotBlank
    @Size(min = 1, max = 25)
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
