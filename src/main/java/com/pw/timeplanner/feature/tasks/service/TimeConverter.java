package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@AllArgsConstructor
public class TimeConverter {

    private final TasksProperties properties;

    public Double timeToNumber(LocalTime time) {
        if (time == null) return null;
        return time.getHour() + time.getMinute() / 60.0;
    }

    public LocalTime numberToTime(Double time) {
        if (time == null) return null;
        double minutes = (time - time.intValue()) * 60;
        return LocalTime.of(time.intValue(), (int) minutes);
    }

    public Double getTimeRangeEnd(LocalTime end) {
        if (end.getHour() == 23 && end.getMinute() == 59) {
            return 24.0;
        }
        return timeToNumber(end);
    }

    public double getDurationHours(TaskEntity e) {
        if (e.getDurationMin() != null) return e.getDurationMin() / 60.0;
        return this.properties.getDefaultDurationMinutes() / 60.0;
    }
}
