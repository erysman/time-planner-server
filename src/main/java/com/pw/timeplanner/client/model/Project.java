package com.pw.timeplanner.client.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

@Value
@Builder
public class Project implements Serializable {
    UUID id;

    @NotBlank
    String name;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    Double timeRangeStart;

//    @Schema(type = "String", pattern = "HH:mm", nullable = true)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    Double timeRangeEnd;

}
