package com.pw.timeplanner.feature.tasks.service

import com.pw.timeplanner.config.TasksProperties
import com.pw.timeplanner.feature.banned_ranges.service.BannedRangesService
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity
import com.pw.timeplanner.feature.tasks.entity.TaskEntity
import com.pw.timeplanner.feature.tasks.repository.TasksRepository
import com.pw.timeplanner.scheduling_client.SchedulingServerClient
import com.pw.timeplanner.scheduling_client.model.ScheduleTasksResponse
import com.pw.timeplanner.scheduling_client.model.ScheduledTask
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalTime

class ScheduleServiceSpec extends Specification {

    TimeConverter timeConverter = new TimeConverter()
    def orderService = Mock(TasksDayOrderService)
    def tasksRepository = Mock(TasksRepository)
    def bannedRangesService = Mock(BannedRangesService)
    def client = Mock(SchedulingServerClient)
    TasksProperties properties = Mock({ getDefaultDurationMinutes() >> defaultDurationMin })
    ScheduleService scheduleService = new ScheduleService(orderService, tasksRepository, bannedRangesService, client, properties, timeConverter)

    static final String userId = "testUser"
    static final int defaultDurationMin = 60
    static final LocalDate day = LocalDate.now()

    def "schedule method schedules tasks correctly"() {
        given: "tasks and projects"
            def runId = UUID.randomUUID()
            def project1 = ProjectEntity.builder().id(UUID.randomUUID()).name("Project 1").build()
            def task1 = TaskEntity.builder().id(UUID.randomUUID()).name("Task 1").dayOrder(0).userId(userId).project(project1).build()
            def task2 = TaskEntity.builder().id(UUID.randomUUID()).name("Task 2").dayOrder(1).userId(userId).project(project1).build()
            def scheduledTask1 = ScheduledTask.builder().id(task1.id).startTime(9.0).build()
            def mockResponse = ScheduleTasksResponse.builder().scheduledTasks([scheduledTask1]).runId(runId).build()
        and: "mocking repository and client interactions"
            tasksRepository.findAndLockAllByUserIdAndStartDayWithProjects(userId, day) >> [task1, task2]
            bannedRangesService.getBannedRanges(userId) >> []
            client.scheduleTasks(_, _, _) >> mockResponse
        when: "schedule method is called"
            scheduleService.schedule(userId, day)
        then: "scheduled tasks are updated correctly"
            1 * orderService.updateOrder(userId, day)
            task1.with {
                it.startTime == timeConverter.numberToTime(scheduledTask1.startTime)
                it.durationMin == defaultDurationMin
                it.autoScheduled == true
                it.scheduleRunId == runId
            }
            task2.startTime == null
    }

    def "schedule method doesn't calls client, if tasks are empty"() {
        given: "no tasks"
            tasksRepository.findAndLockAllByUserIdAndStartDayWithProjects(userId, day) >> []
        when: "schedule method is called"
            scheduleService.schedule(userId, day)
        then: "client is not called"
            0 * client.scheduleTasks(_, _, _)
            0 * orderService.updateOrder(userId, day)
    }

    def "revokeSchedule method modifies autoScheduled tasks"() {
        given: "tasks and projects"
            def runId = UUID.randomUUID()
            def project1 = ProjectEntity.builder().id(UUID.randomUUID()).name("Project 1").build()
            def task1 = TaskEntity.builder().id(UUID.randomUUID()).name("Task 1")
                    .startDay(day).startTime(LocalTime.of(9, 0, 0))
                    .autoScheduled(true).scheduleRunId(runId).durationMin(defaultDurationMin)
                    .userId(userId).project(project1).build()
            def task2 = TaskEntity.builder().id(UUID.randomUUID()).name("Task 2")
                    .startDay(day).startTime(LocalTime.of(10, 0, 0))
                    .autoScheduled(false).durationMin(defaultDurationMin)
                    .userId(userId).project(project1).build()
        and: "mocking repository and client interactions"
            tasksRepository.findAndLockAllByUserIdAndStartDay(userId, day) >> [task1, task2]
        when: "schedule method is called"
            scheduleService.revokeSchedule(userId, day)
        then: "scheduled tasks are updated correctly"
            1 * orderService.updateOrder(userId, day)
            task1.with {
                it.startTime == null
                it.dayOrder == 0
                it.autoScheduled == false
                it.scheduleRunId == null
            }
            task2.with {
                startTime == !null
                dayOrder == null
                autoScheduled == false
                scheduleRunId == null
            }

    }
}
