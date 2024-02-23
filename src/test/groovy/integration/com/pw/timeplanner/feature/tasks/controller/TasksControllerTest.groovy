package integration.com.pw.timeplanner.feature.tasks.controller

import com.pw.timeplanner.config.TasksProperties
import com.pw.timeplanner.feature.tasks.api.TasksResource
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity
import com.pw.timeplanner.feature.tasks.entity.TaskEntity
import com.pw.timeplanner.feature.tasks.repository.ProjectsRepository
import com.pw.timeplanner.feature.tasks.repository.TasksRepository
import integration.UserInitializedSpecification
import org.springframework.beans.factory.annotation.Autowired

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import static org.apache.commons.lang3.RandomStringUtils.random
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TasksControllerTest extends UserInitializedSpecification {

    static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    static def today = LocalDate.now().format(dateFormat)
    static def yesterday = LocalDate.now().minusDays(1).format(dateFormat)
    static def tomorrow = LocalDate.now().plusDays(1).format(dateFormat)

    @Autowired
    TasksRepository tasksRepository

    @Autowired
    ProjectsRepository projectsRepository

    @Autowired
    TasksProperties tasksProperties

    def cleanup() {
        tasksRepository.deleteAll()
    }

    def "should create task"() {
        given:
            def json = buildCreateTaskBody("test", "10:15", today, null)
        when:
            def response = mockMvc.perform(post(TasksResource.RESOURCE_PATH).contentType(APPLICATION_JSON).content(json)
                    .with(userIdJwt()))
        then:
            def tasks = tasksRepository.findAllWithProjectByUserIdAndStartDay(USER_ID, LocalDate.now())
            tasks.size() == 1
            def createdTask = tasks[0]
            createdTask.with {
                assert it.name == "test"
                assert it.startTime == LocalTime.of(10, 15)
                assert it.startDay == LocalDate.now()
                assert it.project == defaultProject
            }
            response.andExpect(status().isCreated())
                    .andExpect(header().string("Location", equalTo("/${createdTask.id}".toString())))
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath('$.id', equalTo(createdTask.id.toString())))
                    .andExpect(jsonPath('$.projectId').exists())
                    .andExpect(jsonPath('$.name', equalTo("test")))
                    .andExpect(jsonPath('$.startTime', equalTo("10:15")))
                    .andExpect(jsonPath('$.startDay', equalTo(today)))

    }

    def "should return error when trying to create task with invalid data"() {
        given:
            def json = buildCreateTaskBody(name, startTime, startDay, projectId)
        when:
            def response = mockMvc.perform(post(TasksResource.RESOURCE_PATH)
                    .contentType(APPLICATION_JSON)
                    .content(json)
                    .with(userIdJwt()))
        then:
            response.andExpect(status().is(status))
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.detail", containsString(violation)))
            tasksRepository.findAll().size() == 0
        where:
            name   | startTime | startDay | projectId              | status | field       | violation
            "name" | "28:00"   | null     | null                   | 400    | "startTime" | 'JSON parse error: Cannot deserialize value of type `java.time.LocalTime` from String "28:00"'
            "name" | null      | null     | "${UUID.randomUUID()}" | 404    | "projectId" | "Resource /projects with id ${projectId} not found"
    }

    def "should return 400 when trying to create task with invalid data"() {
        given:
            def json = buildCreateTaskBody(name, startTime, startDay, projectId, durationMin)
        when:
            def response = mockMvc.perform(post(TasksResource.RESOURCE_PATH)
                    .contentType(APPLICATION_JSON)
                    .content(json)
                    .with(userIdJwt()))
        then:
            response.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.violations[?(@.field == '${field}')].message", hasItem(violation)))
            tasksRepository.findAll().size() == 0
        where:
            name             | startTime | startDay  | durationMin | projectId | field         | violation
            null             | null      | null      | null        | null      | "name"        | "must not be blank"
            ""               | null      | null      | null        | null      | "name"        | "must not be blank"
            "${random(160)}" | null      | null      | null        | null      | "name"        | "size must be between 1 and 150"
            "name"           | "10:13"   | null      | null        | null      | "startTime"   | "must be multiple of '15'"
            "name"           | null      | null      | -30         | null      | "durationMin" | "must be greater than or equal to 30 minutes"
            "name"           | null      | null      | 16          | null      | "durationMin" | "must be greater than or equal to 30 minutes"
            "name"           | null      | null      | 47          | null      | "durationMin" | "must be multiple of '15'"
            "name"           | null      | yesterday | null        | null      | "startDay"    | "must be a date in the present or in the future"
    }

    private static String getWrongTime() {
        def now = LocalTime.now().plusMinutes(5)
        def w = now.plusMinutes(now.getMinute() % 15 == 0 ? 1 : 0)
        timeFormat.format(w)
    }

    def "should get task"() {
        given:
            def task = tasksRepository.save(TaskEntity.builder()
                    .name("test")
                    .startDay(LocalDate.now())
                    .startTime(LocalTime.of(10, 15))
                    .project(defaultProject)
                    .userId(USER_ID)
                    .build())
        when:
            def response = mockMvc.perform(get(TasksResource.RESOURCE_PATH + "/${task.id}").with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath('$.id', equalTo(task.id.toString())))
                    .andExpect(jsonPath('$.projectId', equalTo(task.project.id.toString())))
                    .andExpect(jsonPath('$.name', equalTo(task.name)))
                    .andExpect(jsonPath('$.startTime', equalTo("10:15")))
                    .andExpect(jsonPath('$.startDay', equalTo(today)))
    }

    def "should return 404 when task does not exist"() {
        given:
            tasksRepository.save(TaskEntity.builder()
                    .name("test")
                    .startDay(LocalDate.now())
                    .startTime(LocalTime.of(10, 15))
                    .project(defaultProject)
                    .userId(USER_ID)
                    .build())
            def taskId = UUID.randomUUID()
        when:
            def response = mockMvc.perform(get(TasksResource.RESOURCE_PATH + "/${taskId}").with(userIdJwt()))
        then:
            response.andExpect(status().isNotFound())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.detail", containsString("Resource /tasks with id ${taskId} not found")))
    }

    def "should update task"() {
        given:
            def task = tasksRepository.save(TaskEntity.builder()
                    .name("test")
                    .startDay(LocalDate.now())
                    .startTime(LocalTime.of(10, 15))
                    .project(defaultProject)
                    .userId(USER_ID)
                    .isUrgent(false)
                    .isUrgent(false)
                    .build())
            def json = buildUpdateTaskBody(name, startTime, startDay, projectId, durationMin, isUrgent, isImportant)
        when:
            def response = mockMvc.perform(patch(TasksResource.RESOURCE_PATH + "/${task.id}")
                    .contentType(APPLICATION_JSON)
                    .content(json)
                    .with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath('$.id', equalTo(task.id.toString())))
                    .andExpect(jsonPath('$.projectId', equalTo(defaultProject.id.toString())))
                    .andExpect(jsonPath('$.name', equalTo(name)))
                    .andExpect(jsonPath('$.startTime', equalTo(startTime)))
                    .andExpect(jsonPath('$.startDay', equalTo(startDay)))
                    .andExpect(jsonPath('$.durationMin', equalTo(durationMin)))
                    .andExpect(jsonPath('$.isUrgent', equalTo(isUrgent)))
                    .andExpect(jsonPath('$.isImportant', equalTo(isImportant)))
        where:
            name    | startTime | startDay | durationMin | projectId | isUrgent | isImportant
            "test2" | "10:30"   | tomorrow | 90          | null      | true     | true
    }

    def "should update task's project"() {
        given:
            def project = projectsRepository.save(ProjectEntity.builder().name("Project 1").userId(USER_ID).build())
            def task = tasksRepository.save(TaskEntity.builder()
                    .name("test")
                    .project(defaultProject)
                    .userId(USER_ID)
                    .build())
            def json = "{\"projectId\": \"${project.id}\"}"
        when:
            def response = mockMvc.perform(patch(TasksResource.RESOURCE_PATH + "/${task.id}")
                    .contentType(APPLICATION_JSON)
                    .content(json)
                    .with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath('$.id', equalTo(task.id.toString())))
                    .andExpect(jsonPath('$.projectId', equalTo(project.id.toString())))
        cleanup:
            projectsRepository.delete(project)
    }

    def "should return error when trying to update task with invalid data"() {
        given:
            def task = tasksRepository.save(TaskEntity.builder()
                    .name("test")
                    .startDay(LocalDate.now())
                    .startTime(LocalTime.of(10, 15))
                    .project(defaultProject)
                    .userId(USER_ID)
                    .build())
            def json = buildUpdateTaskBody(name, startTime, startDay, projectId, durationMin, null, null)
        when:
            def response = mockMvc.perform(patch(TasksResource.RESOURCE_PATH + "/${task.id}")
                    .contentType(APPLICATION_JSON)
                    .content(json)
                    .with(userIdJwt()))
        then:
            response.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.violations[?(@.field == '${field}')].message", hasItem(violation)))
            tasksRepository.findById(task.id).orElseThrow().with {
                assert it.name == task.name
                assert it.project.id == task.project.id
                assert it.startDay == task.startDay
                assert it.startTime == task.startTime
                assert it.durationMin == task.durationMin
            }
        where:
            name             | startTime | startDay  | durationMin | projectId | field         | violation
            ""               | null      | null      | null        | null      | "name"        | "must be null or not blank"
            "${random(160)}" | null      | null      | null        | null      | "name"        | "size must be between 1 and 150"
            null             | "10:13"   | null      | null        | null      | "startTime"   | "must be multiple of '15'"
            null             | null      | null      | -30         | null      | "durationMin" | "must be greater than or equal to 30 minutes"
            null             | null      | null      | 16          | null      | "durationMin" | "must be greater than or equal to 30 minutes"
            null             | null      | null      | 47          | null      | "durationMin" | "must be multiple of '15'"
            null             | null      | yesterday | null        | null      | "startDay"    | "must be a date in the present or in the future"
    }

    def "should delete task"() {
        given:
            def task = tasksRepository.save(TaskEntity.builder()
                    .name("test")
                    .project(defaultProject)
                    .userId(USER_ID)
                    .build())
        when:
            def response = mockMvc.perform(delete(TasksResource.RESOURCE_PATH + "/${task.id}").with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
            tasksRepository.findById(task.id).isEmpty()
    }

    def "should return 404 when trying to delete non-existent task"() {
        given:
            def task = tasksRepository.save(TaskEntity.builder()
                    .name("test")
                    .project(defaultProject)
                    .userId(USER_ID)
                    .build())
        when:
            def response = mockMvc.perform(delete(TasksResource.RESOURCE_PATH + "/${UUID.randomUUID()}").with(userIdJwt()))
        then:
            response.andExpect(status().isNotFound())
            tasksRepository.findById(task.id).isPresent()
    }

    private static GString buildCreateTaskBody(name, startTime, startDay, projectId, durationMin = null) {
        """
        {
            "name": ${name == null ? null : "\"${name}\""},
            "startTime": ${startTime == null ? null : "\"${startTime}\""},
            "startDay": ${startDay == null ? null : "\"${startDay}\""},
            "projectId": ${projectId == null ? null : "\"${projectId}\""},
            "durationMin": ${durationMin}
        }
        """
    }

    private static GString buildUpdateTaskBody(name, startTime, startDay, projectId, durationMin, isUrgent, isImportant) {
        """
        {
            "name": ${name == null ? null : "\"${name}\""},
            "startTime": ${startTime == null ? null : "\"${startTime}\""},
            "startDay": ${startDay == null ? null : "\"${startDay}\""},
            "projectId": ${projectId == null ? null : "\"${projectId}\""},
            "durationMin": ${durationMin},
            "isUrgent": ${isUrgent},
            "isImportant": ${isImportant}
        }
        """
    }

}
