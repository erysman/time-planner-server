package com.pw.timeplanner.feature.tasks.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pw.timeplanner.feature.tasks.validation.NullOrGreaterThanMinimumDuration;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskDTO implements Serializable {

    @NotBlank
    String name;

    @Schema(type = "String", pattern = "yyyy-MM-dd", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate startDay;

    @Schema(type = "String", pattern = "HH:mm", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime startTime;

    @NullOrGreaterThanMinimumDuration
    Integer durationMin;
}
