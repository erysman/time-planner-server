package com.pw.timeplanner.feature.banned_ranges.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

@Value
@Builder
public class BannedRangeDTO implements Serializable {
    @NotNull
    UUID id;

    @NotNull
    @Schema(type = "String", pattern = "HH:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime startTime;

    @NotNull
    @Schema(type = "String", pattern = "HH:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime endTime;

}
