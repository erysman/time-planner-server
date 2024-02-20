package com.pw.timeplanner.feature.tasks.service

import com.pw.timeplanner.feature.tasks.entity.TaskEntity
import com.pw.timeplanner.feature.tasks.repository.TasksRepository
import com.pw.timeplanner.feature.tasks.service.exceptions.ListOrderException
import org.openapitools.jackson.nullable.JsonNullable
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate
import java.time.LocalTime

class TasksOrderServiceTest extends Specification {

    TasksRepository tasksRepository = Mock(TasksRepository)
    TasksOrderService service = new TasksOrderService(tasksRepository)

    final static userId = "userId"
    final static day = LocalDate.now()

    def "should change order of tasks"() {
        given:
            def task0 = TaskEntity.builder().id(UUID.randomUUID()).dayOrder(0).build()
            def task1 = TaskEntity.builder().id(UUID.randomUUID()).dayOrder(1).build()
            def task2 = TaskEntity.builder().id(UUID.randomUUID()).dayOrder(2).build()
            def tasks = [task0, task1, task2]
            def newOrder = [task1.id, task2.id, task0.id]
        when:
            def returnedOrder = service.reorderTasksForDay(userId, day, newOrder)
        then:
            1 * tasksRepository.findAndLockTasksWithDayOrder(userId, day) >> tasks
            task1.dayOrder == 0
            task2.dayOrder == 1
            task0.dayOrder == 2
            returnedOrder == newOrder
    }

    def "should throw OrderConflictException when given order contains wrong ids"() {
        given:
            def task0 = TaskEntity.builder().id(taskIds[0]).dayOrder(0).build()
            def task1 = TaskEntity.builder().id(taskIds[1]).dayOrder(1).build()
            def task2 = TaskEntity.builder().id(taskIds[2]).dayOrder(2).build()
            def tasks = [task0, task1, task2]
        when:
            service.reorderTasksForDay(userId, day, newOrder)
        then:
            1 * tasksRepository.findAndLockTasksWithDayOrder(userId, day) >> tasks
            thrown(ListOrderException)
        where:
            taskIds                                                   | newOrder
            [UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()] | [taskIds[2], taskIds[1], taskIds[0], UUID.randomUUID()]
            [UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()] | [taskIds[2], taskIds[1]]
            [UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()] | [taskIds[2], taskIds[1], taskIds[1], taskIds[0]]
    }

    def "should set order for project and startDay"() {
        given:
            def taskEntity = TaskEntity.builder().id(UUID.randomUUID()).startDay(day).build()
        when:
            service.setOrderForDayAndProject(userId, taskEntity)
        then:
            1 * tasksRepository.findLastDayOrder(userId, day) >> Optional.of(maxDayOrder)
            taskEntity.dayOrder == maxDayOrder + 1
        where:
            maxDayOrder << [0, 5, 10, 99]
    }

    def "should not set order for project and startDay, when startTime is present"() {
        given:
            def taskEntity = TaskEntity.builder().id(UUID.randomUUID()).startDay(startDay).startTime(startTime).build()
        when:
            service.setOrderForDayAndProject(userId, taskEntity)
        then:
            0 * tasksRepository.findLastDayOrder(userId, day)
        where:
            startDay        | startTime
            null            | LocalTime.now()
            LocalDate.now() | LocalTime.now()
    }

    def "should unset day order if entity has it"() {
        given:
            def dayOrder = 5
            def taskEntity = TaskEntity.builder().id(UUID.randomUUID()).startDay(day).dayOrder(dayOrder).build()
        when:
            service.unsetDayOrder(userId, taskEntity)
        then:
            taskEntity.dayOrder == null
            1 * tasksRepository.shiftOrderOfAllTasksAfterDeletedOne(userId, taskEntity.getStartDay(), dayOrder);
    }

    def "should not unset day order"() {
        given:
            def dayOrder = 5
            def taskEntity = TaskEntity.builder().id(UUID.randomUUID()).startDay(day).dayOrder(dayOrder).build()
        when:
            service.unsetDayOrder(userId, taskEntity)
        then:
            taskEntity.dayOrder == null
            1 * tasksRepository.shiftOrderOfAllTasksAfterDeletedOne(userId, taskEntity.getStartDay(), dayOrder);
    }

    def "should update tasks order for day"() {
        when:
            service.updateDayOrder(userId, day)
        then:
            1 * tasksRepository.findAndLockAllByUserIdAndStartDayAndStartTimeIsNull(userId, day) >> tasks
            tasks.every { it.dayOrder == expectedOrder[tasks.indexOf(it)] }
        where:
            tasks                                                                     | expectedOrder
            [buildOrderTask(0), buildOrderTask(2), buildOrderTask(10)]                | [0, 1, 2]
            [buildOrderTask(0), buildOrderTask(5), buildOrderTask(1)]                 | [0, 2, 1]
            [buildOrderTask(0), buildOrderTask(), buildOrderTask(10)]                 | [0, 2, 1]
            [buildOrderTask(0), buildOrderTask(10), buildOrderTask()]                 | [0, 1, 2]
            [buildOrderTask(0), buildOrderTask(), buildOrderTask(), buildOrderTask()] | [0, 1, 2, 3]
            [buildOrderTask(), buildOrderTask(), buildOrderTask()]                    | [0, 1, 2]
            [buildOrderTask(0), buildOrderTask(), buildTimeTask()]                    | [0, 1, null]
    }

    @Unroll
    def "should set task's day order for the same day when updating time"() {
        given:
            tasksRepository.findLastDayOrder(userId, day) >> Optional.empty()
            def task = TaskEntity.builder().id(UUID.randomUUID()).startDay(day)
                    .startTime(existingStartTime).dayOrder(existingDayOrder).build()
        when:
            service.updateDayOrder(userId, task, JsonNullable.of(day), updateStartTime)
        then:
            task.dayOrder == expectedDayOrder
        where:
            existingDayOrder | existingStartTime | updateStartTime                  | expectedDayOrder
            null             | LocalTime.now()   | JsonNullable.of(LocalTime.now()) | null // task with time, update time
            null             | LocalTime.now()   | JsonNullable.of(null)            | 0 // task with time, remove time
            null             | LocalTime.now()   | null                             | null // task with time, don't change time
            0                | null              | JsonNullable.of(LocalTime.now()) | null // task without time, update time
            0                | null              | JsonNullable.of(null)            | 0 // task without time, remove time
            0                | null              | null                             | 0 // task without time, don't change time
    }

    @Unroll
    def "should unset task's day order when updating day and time"() {
        given:
            tasksRepository.findLastDayOrder(userId, day) >> Optional.empty()
            tasksRepository.findLastDayOrder(userId, day.plusDays(1)) >> Optional.of(1)
            def task = TaskEntity.builder().id(UUID.randomUUID()).startDay(day).startTime(null).dayOrder(existingOrder).build()
        when:
            service.updateDayOrder(userId, task, updateStartDay, updateStartTime)
        then:
            task.dayOrder == expectedOrder
            1 * tasksRepository.shiftOrderOfAllTasksAfterDeletedOne(userId, day, existingOrder)
        where:
            existingOrder | updateStartDay                   | updateStartTime                  | expectedOrder
            0             | JsonNullable.of(null)            | null                             | null
            0             | JsonNullable.of(null)            | JsonNullable.of(null)            | null
            0             | JsonNullable.of(day.plusDays(1)) | JsonNullable.of(LocalTime.now()) | null
            0             | JsonNullable.of(day.plusDays(1)) | JsonNullable.of(LocalTime.now()) | null
            0             | JsonNullable.of(day.plusDays(1)) | JsonNullable.of(null)            | 2 // task without time, remove time
            0             | JsonNullable.of(day.plusDays(1)) | null                             | 2 // task without time, don't change time
    }

    @Unroll
    def "should set task's day order when updating day and time"() {
        given:
            tasksRepository.findLastDayOrder(userId, day) >> Optional.empty()
            tasksRepository.findLastDayOrder(userId, day.plusDays(1)) >> Optional.of(1)
            def task = TaskEntity.builder().id(UUID.randomUUID()).startDay(existingDay).startTime(existingStartTime).dayOrder(existingOrder).build()
        when:
            service.updateDayOrder(userId, task, updateStartDay, updateStartTime)
        then:
            task.dayOrder == expectedOrder
        where:
            existingOrder | existingDay | existingStartTime | updateStartDay                   | updateStartTime                  | expectedOrder
            null          | null        | null              | null                             | JsonNullable.of(null)            | null
            null          | null        | null              | JsonNullable.of(day.plusDays(1)) | JsonNullable.of(LocalTime.now()) | null
            null          | day         | LocalTime.now()   | JsonNullable.of(null)            | JsonNullable.of(null)            | null
            null          | day         | LocalTime.now()   | JsonNullable.of(day.plusDays(1)) | JsonNullable.of(null)            | 2
            null          | day         | LocalTime.now()   | null                             | JsonNullable.of(null)            | 0
            null          | null        | null              | JsonNullable.of(day.plusDays(1)) | null                             | 2
            null          | null        | null              | JsonNullable.of(day.plusDays(1)) | JsonNullable.of(null)            | 2
            null          | day         | LocalTime.now()   | JsonNullable.of(day.plusDays(1)) | null                             | null
            null          | day         | LocalTime.now()   | JsonNullable.of(day.plusDays(1)) | JsonNullable.of(LocalTime.now()) | null
            null          | null        | LocalTime.now()   | JsonNullable.of(day.plusDays(1)) | null                             | null
            null          | null        | LocalTime.now()   | JsonNullable.of(day.plusDays(1)) | JsonNullable.of(null)            | 2
    }

    private static TaskEntity buildOrderTask(Integer dayOrder = null) {
        TaskEntity.builder().id(UUID.randomUUID()).startDay(day).dayOrder(dayOrder).build()
    }

    private static TaskEntity buildTimeTask(LocalTime startTime = LocalTime.now()) {
        TaskEntity.builder().id(UUID.randomUUID()).startDay(day).dayOrder(null).startTime(startTime).build()
    }
}
