package com.pw.timeplanner.feature.tasks.api.dto;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum PriorityDTO {
    IMPORTANT_URGENT(4), IMPORTANT(3), URGENT(2), NORMAL(1);

    private final int value;

    PriorityDTO(int value) {
        this.value = value;
    }

    public static PriorityDTO of(int priority) {
        return Stream.of(PriorityDTO.values())
                .filter(p -> p.getValue() == priority)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
