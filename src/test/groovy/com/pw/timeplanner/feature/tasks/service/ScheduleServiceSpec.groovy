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

class ScheduleServiceSpec extends Specification {

    ScheduleService scheduleService
    TimeConverter timeConverter
    def orderService = Mock(TasksOrderService)
    def tasksRepository = Mock(TasksRepository)
    def bannedRangesService = Mock(BannedRangesService)
    def client = Mock(SchedulingServerClient)
    def properties = Mock(TasksProperties)

    String userId = "testUser"
    LocalDate day = LocalDate.now()

    def setup() {
        properties.getDefaultDurationMinutes() >> 60
        timeConverter = new TimeConverter(properties)
        scheduleService = new ScheduleService(orderService, tasksRepository, bannedRangesService, client, properties, timeConverter)
    }

    def "schedule method schedules tasks correctly"() {
        given: "tasks and projects"
            def runId = UUID.randomUUID()
            def project1 = buildProject("Project 1")
            def task1 = buildTask(project1, "Task 1", 0)
            def task2 = buildTask(project1, "Task 2", 1)
            def scheduledTask1 = buildScheduledTask(task1.id, 9.0)
        and: "mocking repository and client interactions"
            tasksRepository.findAndLockAllByUserIdAndStartDayWithProjects(userId, day) >> [task1, task2]
            bannedRangesService.getBannedRanges(userId) >> []
            client.scheduleTasks(_, _, _) >> ScheduleTasksResponse.builder()
                    .scheduledTasks([scheduledTask1]).runId(runId).build()
        when: "schedule method is called"
            scheduleService.schedule(userId, day)
        then: "scheduled tasks are updated correctly"
            1 * orderService.updateDayOrder(userId, day)
            timeConverter.timeToNumber(task1.startTime) == scheduledTask1.startTime
            task1.durationMin == 60
            task1.autoScheduled == true
            task1.scheduleRunId == runId
            task2.startTime == null
    }

    private ScheduledTask buildScheduledTask(UUID id, Double startTime) {
        ScheduledTask.builder().id(id).startTime(startTime).build()
    }

    private ProjectEntity buildProject(String name) {
        ProjectEntity.builder().id(UUID.randomUUID()).name(name).build()
    }

    private TaskEntity buildTask(ProjectEntity project1, String name, Integer dayOrder) {
        TaskEntity.builder().id(UUID.randomUUID()).name(name).dayOrder(dayOrder).userId(userId).project(project1).build()
    }
}
