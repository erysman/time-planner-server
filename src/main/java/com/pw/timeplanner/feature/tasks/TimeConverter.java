package com.pw.timeplanner.feature.tasks;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@AllArgsConstructor
class TimeConverter {

    Double timeToNumber(LocalTime time) {
        if (time == null) return null;
        return time.getHour() + time.getMinute() / 60.0;
    }

    LocalTime numberToTime(Double time) {
        if (time == null) return null;
        double minutes = (time - time.intValue()) * 60;
        return LocalTime.of(time.intValue(), (int) minutes);
    }

    Double getTimeRangeEnd(LocalTime end) {
        if (end.getHour() == 23 && end.getMinute() == 59) {
            return 24.0;
        }
        return timeToNumber(end);
    }
}
