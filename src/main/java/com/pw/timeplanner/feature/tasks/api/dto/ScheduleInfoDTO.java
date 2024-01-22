package com.pw.timeplanner.feature.tasks.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
@Value
@Builder
@AllArgsConstructor
public class ScheduleInfoDTO implements Serializable {
    @NotNull
    Boolean isScheduled;
}
