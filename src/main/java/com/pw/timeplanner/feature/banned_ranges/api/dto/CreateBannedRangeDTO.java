package com.pw.timeplanner.feature.banned_ranges.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalTime;

@Value
@Builder
public class CreateBannedRangeDTO implements Serializable {

    @Schema(type = "String", pattern = "HH:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime startTime;

    @Schema(type = "String", pattern = "HH:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime endTime;

}
