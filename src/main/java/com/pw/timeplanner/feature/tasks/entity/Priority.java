package com.pw.timeplanner.feature.tasks.entity;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum Priority {
    IMPORTANT_URGENT(4), IMPORTANT(3), URGENT(2), NORMAL(1);

    private final int value;

    Priority(int value) {
        this.value = value;
    }

    public static Priority of(int priority) {
        return Stream.of(Priority.values())
                .filter(p -> p.getValue() == priority)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
