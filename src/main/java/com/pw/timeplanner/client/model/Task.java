package com.pw.timeplanner.client.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

@Value
@Builder
public class Task implements Serializable {
    UUID id;

    UUID projectId;

    @NotBlank
    String name;

//    @Schema(type = "String", pattern = "HH:mm", nullable = true)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    Double startTime;

    Double duration;

    int priority;
}