package com.pw.timeplanner.client.model;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

@Value
@Builder
public class BannedRange implements Serializable {
    UUID id;

//    @Schema(type = "String", pattern = "HH:mm", nullable = true)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    Double timeRangeStart;

//    @Schema(type = "String", pattern = "HH:mm", nullable = true)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    Double timeRangeEnd;

}
