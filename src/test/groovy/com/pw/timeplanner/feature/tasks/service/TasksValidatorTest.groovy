package com.pw.timeplanner.feature.tasks.service

import com.pw.timeplanner.config.TasksProperties
import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO
import com.pw.timeplanner.feature.tasks.api.dto.UpdateTaskDTO
import com.pw.timeplanner.feature.tasks.service.exceptions.TimeGranularityException
import com.pw.timeplanner.feature.tasks.service.validator.TasksValidator
import org.openapitools.jackson.nullable.JsonNullable
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalTime

class TasksValidatorTest extends Specification {
    TasksProperties tasksProperties = Mock({
        getDefaultDurationMinutes() >> 60
        getTimeGranularityMinutes() >> 15
    })
    TasksValidator tasksValidator = new TasksValidator(tasksProperties)

    def "should validate task creation"() {
        given:
            def createTaskDto = CreateTaskDTO.builder()
                    .name(name)
                    .startDay(startDay)
                    .startTime(startTime)
                    .projectId(UUID.randomUUID())
                    .durationMin(durationMin)
                    .build()
        expect:
            tasksValidator.validate(createTaskDto)
        where:
            name   | startDay        | startTime           | projectId         | durationMin
            "Task" | LocalDate.now() | LocalTime.of(9, 15) | UUID.randomUUID() | 60
    }

    def "should throw on task creation"() {
        given:
            def createTaskDto = CreateTaskDTO.builder()
                    .name(name)
                    .startDay(startDay)
                    .startTime(startTime)
                    .projectId(UUID.randomUUID())
                    .durationMin(durationMin)
                    .build()
        when:
            tasksValidator.validate(createTaskDto)
        then:
            thrown(exception)
        where:
            name   | startDay        | startTime           | projectId         | durationMin | exception
            "Task" | LocalDate.now() | LocalTime.of(9, 13) | UUID.randomUUID() | 60          | TimeGranularityException
    }

    def "should validate task update"() {
        given:
            def updateTaskDto = UpdateTaskDTO.builder()
                    .name(name)
                    .startDay(startDay)
                    .startTime(startTime)
                    .projectId(UUID.randomUUID())
                    .durationMin(durationMin)
                    .build()
        expect:
            tasksValidator.validate(updateTaskDto)
        where:
            name   | startDay                         | startTime                            | projectId         | durationMin
            "Task" | JsonNullable.of(LocalDate.now()) | JsonNullable.of(LocalTime.of(9, 15)) | UUID.randomUUID() | JsonNullable.of(60)
    }

    def "should throw on validate task update"() {
        given:
            def updateTaskDto = UpdateTaskDTO.builder()
                    .name(name)
                    .startDay(startDay)
                    .startTime(startTime)
                    .projectId(UUID.randomUUID())
                    .durationMin(durationMin)
                    .build()
        when:
            tasksValidator.validate(updateTaskDto)
        then:
            thrown(exception)
        where:
            name   | startDay                         | startTime                            | projectId         | durationMin         | exception
            "Task" | JsonNullable.of(LocalDate.now()) | JsonNullable.of(LocalTime.of(9, 13)) | UUID.randomUUID() | JsonNullable.of(60) | TimeGranularityException
    }
}
