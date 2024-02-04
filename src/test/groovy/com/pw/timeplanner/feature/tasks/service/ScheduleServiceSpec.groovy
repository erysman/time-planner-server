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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

import java.time.LocalDate

import static org.mockito.BDDMockito.*

@SpringBootTest
class ScheduleServiceSpec extends Specification {

    @Autowired
    ScheduleService scheduleService

    @MockBean
    TasksOrderService orderService

    @MockBean
    TasksRepository tasksRepository

    @MockBean
    BannedRangesService bannedRangesService

    @MockBean
    SchedulingServerClient client

    @MockBean
    TasksProperties properties

    def "schedule method schedules tasks correctly"() {
        given: "A user ID, a day, and mock tasks"
            String userId = "testUser"
            LocalDate day = LocalDate.now()
            List<ProjectEntity> projectEntities = [
                    new ProjectEntity(id: UUID.randomUUID(), name: "Project 1")
            ]
            List<TaskEntity> mockTaskEntities = [
                    new TaskEntity(id: UUID.randomUUID(), name: "Task 1", durationMin: 60, startTime: null, dayOrder: 0, userId: userId),
            ]
            List<ScheduledTask> scheduledTasks = [
                    new ScheduledTask(id: mockTaskEntities[0].id, startTime: 9.0),
            ]
            ScheduleTasksResponse mockResponse = new ScheduleTasksResponse(scheduledTasks: scheduledTasks, runId: UUID.randomUUID())

        and: "Mocking repository and client interactions"
            mockTaskEntities.each { it.project = projectEntities[0] }
            when(tasksRepository.findAndLockAllByUserIdAndStartDay(userId, day)).thenReturn(mockTaskEntities)
            when(bannedRangesService.getBannedRanges(userId)).thenReturn([])
            when(client.scheduleTasks(_, _, _)).thenReturn(mockResponse)
            when(properties.getDefaultDurationMinutes()).thenReturn(60)

        when: "schedule method is called"
            scheduleService.schedule(userId, day)

        then: "Scheduled tasks are updated correctly"
            1 * tasksRepository.saveAll(_)
            1 * orderService.updateDayOrder(_, _, _, _)
    }
}
