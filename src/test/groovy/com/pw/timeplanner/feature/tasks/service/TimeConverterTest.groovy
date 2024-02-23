package com.pw.timeplanner.feature.tasks.service


import spock.lang.Specification

import java.time.LocalTime

class TimeConverterTest extends Specification {

    def timeConverter = new TimeConverter()

    def "timeToNumber method converts time to number correctly"() {
        expect:
            timeConverter.timeToNumber(time) == timeDouble
        where:
            time                    | timeDouble
            LocalTime.of(0, 0, 0)   | 0.0
            LocalTime.of(9, 45, 0)  | 9.75
            LocalTime.of(12, 0, 0)  | 12.0
            LocalTime.of(23, 59, 0) | 23.983333333333334
    }

    def "numberToTime method converts time to number correctly"() {
        expect:
            timeConverter.numberToTime(timeDouble) == time
        where:
            timeDouble         | time
            0.0                | LocalTime.of(0, 0, 0)
            9.75               | LocalTime.of(9, 45, 0)
            12.0               | LocalTime.of(12, 0, 0)
            23.983333333333334 | LocalTime.of(23, 59, 0)
    }

    def "getTimeRangeEnd method returns correct time range end"() {
        expect:
            timeConverter.getTimeRangeEnd(time) == timeDouble
        where:
            time                    | timeDouble
            LocalTime.of(0, 0, 0)   | 0.0
            LocalTime.of(9, 45, 0)  | 9.75
            LocalTime.of(12, 0, 0)  | 12.0
            LocalTime.of(23, 59, 0) | 24.0
    }
}
