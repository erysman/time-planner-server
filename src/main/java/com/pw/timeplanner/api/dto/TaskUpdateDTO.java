package com.pw.timeplanner.api.dto;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Value
@Builder
public class TaskUpdateDTO implements Serializable {
    String name;
    LocalDate startDate;
    LocalTime startTime;
    Integer durationMin;
}
