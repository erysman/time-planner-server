package com.pw.timeplanner.api.dto;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Value
@Builder
public class TaskDTO implements Serializable {
    UUID id;
    String name;
    LocalDate startDate;
    LocalTime startTime;
    Integer durationMin;
}
