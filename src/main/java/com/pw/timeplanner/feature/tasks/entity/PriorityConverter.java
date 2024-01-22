package com.pw.timeplanner.feature.tasks.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class PriorityConverter implements AttributeConverter<Priority, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Priority priority) {
        if (priority == null) {
            return null;
        }
        return priority.getValue();
    }

    @Override
    public Priority convertToEntityAttribute(Integer value) {
        if (value == null) {
            return null;
        }

        return Stream.of(Priority.values())
                .filter(c -> c.getValue() == value)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}