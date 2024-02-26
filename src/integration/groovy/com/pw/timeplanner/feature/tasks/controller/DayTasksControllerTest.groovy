package com.pw.timeplanner.feature.tasks.controller

import com.pw.timeplanner.UserInitializedSpecification
import com.pw.timeplanner.config.TasksProperties
import com.pw.timeplanner.feature.banned_ranges.BannedRangesService
import com.pw.timeplanner.feature.banned_ranges.api.dto.CreateBannedRangeDTO
import com.pw.timeplanner.feature.tasks.Task
import com.pw.timeplanner.feature.tasks.TasksRepository
import com.pw.timeplanner.feature.tasks.api.DayTasksResource
import com.pw.timeplanner.scheduling_client.SchedulingServerClient
import com.pw.timeplanner.scheduling_client.model.ScheduleTasksResponse
import com.pw.timeplanner.scheduling_client.model.ScheduledTask
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class DayTasksControllerTest extends UserInitializedSpecification {

    static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    static def today = LocalDate.now().format(dateFormat)
    static def randomTaskId = UUID.randomUUID()

    @Autowired
    TasksRepository tasksRepository

    @Autowired
    BannedRangesService bannedRangesService

    @Autowired
    TasksProperties tasksProperties

    @SpringBean
    SchedulingServerClient schedulingServerClient = Mock()

    def cleanup() {
        tasksRepository.deleteAll()
    }

    def "should get tasks by start day"() {
        given:
            def task1 = tasksRepository.save(Task.builder()
                                                 .name("test1")
                                                 .startDay(LocalDate.now())
                                                 .startTime(LocalTime.of(10, 15))
                                                 .projectId(defaultProject)
                                                 .userId(USER_ID)
                                                 .build())
            def task2 = tasksRepository.save(Task.builder()
                                                 .name("test2")
                                                 .startDay(LocalDate.now())
                                                 .startTime(LocalTime.of(10, 15))
                                                 .projectId(defaultProject)
                                                 .userId(USER_ID)
                                                 .build())
            def taskTomorrow = tasksRepository.save(Task.builder()
                                                        .name("test3")
                                                        .startDay(LocalDate.now().plusDays(1))
                                                        .startTime(LocalTime.of(10, 15))
                                                        .projectId(defaultProject)
                                                        .userId(USER_ID)
                                                        .build())

        when:
            def response = mockMvc.perform(get(DayTasksResource.RESOURCE_PATH, today).with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath('$.length()', equalTo(2)))
                    .andExpect(jsonPath('$.[*].id', hasItem(task1.id.toString())))
                    .andExpect(jsonPath('$.[*].id', hasItem(task2.id.toString())))
    }

    def "should return empty list when getting tasks by start day"() {
        given:
            def taskTomorrow = tasksRepository.save(Task.builder()
                                                        .name("test3")
                                                        .startDay(LocalDate.now().plusDays(1))
                                                        .startTime(LocalTime.of(10, 15))
                                                        .projectId(defaultProject)
                                                        .userId(USER_ID)
                                                        .build())
        when:
            def response = mockMvc.perform(get(DayTasksResource.RESOURCE_PATH, today).with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath('$.length()', equalTo(0)))
    }

    def "should get tasks order by start day"() {
        given:
            def task1 = tasksRepository.save(Task.builder()
                                                 .name("test1")
                                                 .startDay(LocalDate.now())
                                                 .dayOrder(0)
                                                 .projectId(defaultProject)
                                                 .userId(USER_ID)
                                                 .build())
            def task2 = tasksRepository.save(Task.builder()
                                                 .name("test2")
                                                 .startDay(LocalDate.now())
                                                 .dayOrder(1)
                                                 .projectId(defaultProject)
                                                 .userId(USER_ID)
                                                 .build())
            def taskTomorrow = tasksRepository.save(Task.builder()
                                                        .name("test3")
                                                        .startDay(LocalDate.now().plusDays(1))
                                                        .startTime(LocalTime.of(10, 15))
                                                        .projectId(defaultProject)
                                                        .userId(USER_ID)
                                                        .build())

        when:
            def url = DayTasksResource.RESOURCE_PATH + DayTasksResource.ORDER_PATH
            def response = mockMvc.perform(get(url, today).with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath('$.length()', equalTo(2)))
                    .andExpect(jsonPath('$[0]', equalTo(task1.id.toString())))
                    .andExpect(jsonPath('$[1]', equalTo(task2.id.toString())))
    }

    def "should update tasks order by start day"() {
        given:
            def task1 = tasksRepository.save(Task.builder()
                                                 .name("test1")
                                                 .startDay(LocalDate.now())
                                                 .dayOrder(0)
                                                 .projectId(defaultProject)
                                                 .userId(USER_ID)
                                                 .build())
            def task2 = tasksRepository.save(Task.builder()
                                                 .name("test2")
                                                 .startDay(LocalDate.now())
                                                 .dayOrder(1)
                                                 .projectId(defaultProject)
                                                 .userId(USER_ID)
                                                 .build())
        when:
            def url = DayTasksResource.RESOURCE_PATH + DayTasksResource.ORDER_PATH
            def response = mockMvc.perform(put(url, today).with(userIdJwt()).contentType(APPLICATION_JSON)
                    .content("[\"${task2.id}\",\"${task1.id}\"]"))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath('$.length()', equalTo(2)))
                    .andExpect(jsonPath('$[0]', equalTo(task2.id.toString())))
                    .andExpect(jsonPath('$[1]', equalTo(task1.id.toString())))
            def updatedTasks = tasksRepository.findAll()
            updatedTasks.find { it.id == task1.id }.with {
                assert it.dayOrder == 1
            }
            updatedTasks.find { it.id == task2.id }.with {
                assert it.dayOrder == 0
            }
    }

    def "should throw when trying to update tasks order with missing task id"() {
        given:
            def task1 = saveTaskWithOrder(0)
            def task2 = saveTaskWithOrder(1)
        when:
            def url = DayTasksResource.RESOURCE_PATH + DayTasksResource.ORDER_PATH
            def response = mockMvc.perform(put(url, today).with(userIdJwt()).contentType(APPLICATION_JSON)
                    .content("[\"${task1.id.toString()}\"]"))
        then:
            response.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.violations[*].message", hasItem("Missing task ids: [${task2.id.toString()}]".toString())))
    }

    def "should throw when trying to update tasks order with non-existent task id"() {
        given:
            def task1 = saveTaskWithOrder(0)
            def task2 = saveTaskWithOrder(1)
            def tasksOrder = [task1.id, task2.id, randomTaskId]
        when:
            def url = DayTasksResource.RESOURCE_PATH + DayTasksResource.ORDER_PATH
            def response = mockMvc.perform(put(url, today).with(userIdJwt()).contentType(APPLICATION_JSON)
                    .content("[${tasksOrder.collect { "\"${it.toString()}\"" }.join(",")}]"))
        then:
            response.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.violations[*].message", hasItem("Non-existent task ids: [${randomTaskId.toString()}]".toString())))
    }

    def "should throw when trying to update tasks order with duplicated task ids"() {
        given:
            def task1 = saveTaskWithOrder(0)
            def task2 = saveTaskWithOrder(1)
            def tasksOrder = [task1.id, task2.id, task2.id]
        when:
            def url = DayTasksResource.RESOURCE_PATH + DayTasksResource.ORDER_PATH
            def response = mockMvc.perform(put(url, today).with(userIdJwt()).contentType(APPLICATION_JSON)
                    .content("[${tasksOrder.collect { "\"${it.toString()}\"" }.join(",")}]"))
        then:
            response.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.violations[*].message", hasItem("Tasks ids are not distinct: [${task2.id.toString()}]".toString())))
    }

    private Task saveTaskWithOrder(int dayOrder) {
        tasksRepository.save(Task.builder()
                                 .name(dayOrder.toString())
                                 .startDay(LocalDate.now())
                                 .dayOrder(dayOrder)
                                 .projectId(defaultProject)
                                 .userId(USER_ID)
                                 .build())
    }

    def "should get schedule info"() {
        given:
            tasksRepository.save(Task.builder()
                                     .name("test1")
                                     .startDay(LocalDate.now())
                                     .startTime(LocalTime.of(10, 15))
                                     .autoScheduled(true)
                                     .scheduleRunId(UUID.randomUUID())
                                     .projectId(defaultProject)
                                     .userId(USER_ID)
                                     .build())
        when:
            def response = mockMvc.perform(get(DayTasksResource.RESOURCE_PATH + DayTasksResource.SCHEDULE_PATH, today).with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath('$.isScheduled', equalTo(true)))
    }

    def "should schedule tasks"() {
        given:
            def runId = UUID.randomUUID()
            def task1 = tasksRepository.save(Task.builder()
                                                 .name("test1")
                                                 .startDay(LocalDate.now())
                                                 .projectId(defaultProject)
                                                 .userId(USER_ID)
                                                 .build())
            bannedRangesService.createBannedRange(USER_ID, CreateBannedRangeDTO.builder()
                    .startTime(LocalTime.of(0, 0))
                    .endTime(LocalTime.of(8, 0))
                    .build())
        when:
            def response = mockMvc.perform(post(DayTasksResource.RESOURCE_PATH + DayTasksResource.SCHEDULE_PATH, today).with(userIdJwt()))
        then:
            1 * schedulingServerClient.scheduleTasks(*_) >> ScheduleTasksResponse.builder()
                    .runId(runId)
                    .scheduledTasks([ScheduledTask.builder().id(task1.id).startTime(8.0).build()])
                    .build()
            response.andExpect(status().isOk())
            tasksRepository.findById(task1.id).with {
                assert it.isPresent()
                assert it.get().autoScheduled == true
                assert it.get().scheduleRunId == runId
                assert it.get().startTime == LocalTime.of(8, 0)
            }
    }

    def "should unschedule tasks"() {
        given:
            def task1 = tasksRepository.save(Task.builder()
                                                 .name("test1")
                                                 .startDay(LocalDate.now())
                                                 .autoScheduled(true)
                                                 .scheduleRunId(UUID.randomUUID())
                                                 .startTime(LocalTime.of(10, 15))
                                                 .projectId(defaultProject)
                                                 .userId(USER_ID)
                                                 .build())
        when:
            def response = mockMvc.perform(delete(DayTasksResource.RESOURCE_PATH + DayTasksResource.SCHEDULE_PATH, today).with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
            tasksRepository.findById(task1.id).with {
                assert it.isPresent()
                assert it.get().autoScheduled == false
                assert it.get().startTime == null
            }
    }
}
