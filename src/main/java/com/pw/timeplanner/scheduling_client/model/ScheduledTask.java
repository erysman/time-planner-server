package com.pw.timeplanner.scheduling_client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor
public class ScheduledTask implements Serializable {
    UUID id;

//    @Schema(type = "String", pattern = "HH:mm", nullable = true)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    Double startTime;
}
