package integration.com.pw.timeplanner.feature.tasks.controller

import com.pw.timeplanner.config.TasksProperties
import com.pw.timeplanner.feature.tasks.api.ProjectsResource
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity
import com.pw.timeplanner.feature.tasks.entity.TaskEntity
import com.pw.timeplanner.feature.tasks.repository.ProjectsRepository
import com.pw.timeplanner.feature.tasks.repository.TasksRepository
import integration.UserInitializedSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils

import java.time.LocalTime

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

class ProjectControllerTest extends UserInitializedSpecification {

    @Autowired
    TasksProperties tasksProperties

    @Autowired
    ProjectsRepository projectsRepository

    @Autowired
    TasksRepository tasksRepository

    def "should get all projects"() {
        given:
            def project = projectsRepository.save(ProjectEntity.builder().name("Project 1").userId(USER_ID).build())
        when:
            def response = mockMvc.perform(get(ProjectsResource.RESOURCE_PATH).with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath("\$.length()", equalTo(2)))
                    .andExpect(jsonPath("\$.[*].id", hasItem(project.id.toString())))
                    .andExpect(jsonPath("\$.[*].id", hasItem(defaultProject.id.toString())))
        cleanup:
            projectsRepository.delete(project)
    }

    def "should get project"() {
        given:
            def project = projectsRepository.save(ProjectEntity.builder().name("Project 1").userId(USER_ID).build())
        when:
            def response = mockMvc.perform(get(ProjectsResource.RESOURCE_PATH + "/${project.id.toString()}").with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath("\$.id", equalTo(project.id.toString())))
                    .andExpect(jsonPath("\$.name", equalTo(project.name)))
        cleanup:
            projectsRepository.delete(project)
    }

    def "should return 404 when trying to get non-existent project"() {
        given:
            def id = UUID.randomUUID()
        when:
            def response = mockMvc.perform(get(ProjectsResource.RESOURCE_PATH + "/${id}").with(userIdJwt()))
        then:
            response.andExpect(status().isNotFound())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.detail", containsString("Resource ${ProjectsResource.RESOURCE_PATH} with id ${id.toString()} not found")))
    }

    def "should create project"() {
        given:
            def json = buildCreateProjectBody("Project1", "#FF0000", "08:00", "16:00")
        when:
            def response = mockMvc.perform(post(ProjectsResource.RESOURCE_PATH).contentType(APPLICATION_JSON).content(json).with(userIdJwt()))
        then:
            def createdProject = projectsRepository.findOneByUserIdAndName(USER_ID, "Project1").orElseThrow()
            createdProject.with {
                assert it.color == "#FF0000"
                assert it.scheduleStartTime == LocalTime.of(8, 0)
                assert it.scheduleEndTime == LocalTime.of(16, 0)
                assert it.userId == USER_ID
                assert it.name == "Project1"
            }
            response.andExpect(status().isCreated())
                    .andExpect(header().string("Location", equalTo("/${createdProject.id}".toString())))
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath("\$.id", equalTo(createdProject.id.toString())))
                    .andExpect(jsonPath("\$.name", equalTo("Project1")))
                    .andExpect(jsonPath("\$.color", equalTo("#FF0000")))
                    .andExpect(jsonPath("\$.scheduleStartTime", equalTo("08:00")))
                    .andExpect(jsonPath("\$.scheduleEndTime", equalTo("16:00")))

        cleanup:
            projectsRepository.delete(createdProject)
    }

    def "should throw when trying to create project with existing name"() {
        given:
            def project = projectsRepository.save(ProjectEntity.builder().name("Project1").userId(USER_ID).build())
            def json = buildCreateProjectBody("Project1")
        when:
            def response = mockMvc.perform(post(ProjectsResource.RESOURCE_PATH).contentType(APPLICATION_JSON).content(json).with(userIdJwt()))
        then:
            response.andExpect(status().isConflict())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.detail", containsString("Resource ${ProjectsResource.RESOURCE_PATH} with field 'name': 'Project1' already exists")))
        cleanup:
            projectsRepository.delete(project)
    }

    def "should throw 400 when trying to create project with invalid data"() {
        given:
            def json = buildCreateProjectBody(name, color, scheduleStartTime, scheduleEndTime)
        when:
            def response = mockMvc.perform(post(ProjectsResource.RESOURCE_PATH).contentType(APPLICATION_JSON).content(json).with(userIdJwt()))
        then:
            response.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.violations[?(@.field == '${field}')].message", hasItem(violation)))
        where:
            name                                   | color | scheduleStartTime | scheduleEndTime | field               | violation
            ""                                     | null  | null              | null            | "name"              | "must not be blank"
            RandomStringUtils.randomAlphabetic(50) | null  | null              | null            | "name"              | "size must be between 1 and 25"
            "Project1"                             | null  | "16:00"           | "12:13"         | "scheduleEndTime"   | "must be multiple of '15'"
            "Project1"                             | null  | "16:00"           | "12:00"         | "scheduleStartTime" | "period's start time must be before end time"
    }

    def "should return 400 when trying to create project with invalid data"() {
        given:
            def json = buildCreateProjectBody(name, color, scheduleStartTime, scheduleEndTime)
        when:
            def response = mockMvc.perform(post(ProjectsResource.RESOURCE_PATH).contentType(APPLICATION_JSON).content(json).with(userIdJwt()))
        then:
            response.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.violations[?(@.field == '${field}')].message", hasItem(violation)))
            projectsRepository.findAll().size() == 1
        where:
            name                                   | color | scheduleStartTime | scheduleEndTime | field  | violation
            ""                                     | null  | null              | null            | "name" | "must not be blank"
            RandomStringUtils.randomAlphabetic(50) | null  | null              | null            | "name" | "size must be between 1 and 25"
    }

    def "should delete project and it's tasks"() {
        given:
            def project = projectsRepository.save(ProjectEntity.builder().name("Project 1").userId(USER_ID).build())
            def task = tasksRepository.save(TaskEntity.builder().name("Task 1").project(project).userId(USER_ID).build())
            def task2 = tasksRepository.save(TaskEntity.builder().name("Task 2").project(project).userId(USER_ID).build())
        when:
            def response = mockMvc.perform(delete(ProjectsResource.RESOURCE_PATH + "/${project.id.toString()}").with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
            projectsRepository.findById(project.id).isEmpty()
            tasksRepository.findById(task.id).isEmpty()
            tasksRepository.findById(task2.id).isEmpty()
    }

    def "should not be able to delete default project, should just delete all it's tasks"() {
        given:
            def task = tasksRepository.save(TaskEntity.builder().name("Task 1").project(defaultProject).userId(USER_ID).build())
            def task2 = tasksRepository.save(TaskEntity.builder().name("Task 2").project(defaultProject).userId(USER_ID).build())
        when:
            def response = mockMvc.perform(delete(ProjectsResource.RESOURCE_PATH + "/${defaultProject.id.toString()}").with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
            projectsRepository.findById(defaultProject.id).isEmpty()
            projectsRepository.findOneByUserIdAndName(USER_ID, tasksProperties.getDefaultProjectName()).isPresent()
            tasksRepository.findById(task.id).isEmpty()
            tasksRepository.findById(task2.id).isEmpty()
    }

    def "should return 404 when trying to delete non-existent project"() {
        given:
            def id = UUID.randomUUID()
        when:
            def response = mockMvc.perform(delete(ProjectsResource.RESOURCE_PATH + "/${id}").with(userIdJwt()))
        then:
            response.andExpect(status().isNotFound())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.detail", containsString("Resource ${ProjectsResource.RESOURCE_PATH} with id ${id.toString()} not found")))
    }

    def "should update project"() {
        given:
            def project = projectsRepository.save(ProjectEntity.builder()
                    .name("Project1")
                    .userId(USER_ID)
                    .scheduleStartTime(LocalTime.of(8, 0))
                    .scheduleEndTime(LocalTime.of(16, 0))
                    .color("#FF0000")
                    .build())
            def json = buildCreateProjectBody("Project2", "#000000", "09:00", "18:00")
        when:
            def response = mockMvc.perform(patch(ProjectsResource.RESOURCE_PATH + "/${project.id.toString()}").contentType(APPLICATION_JSON).content(json).with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath("\$.id", equalTo(project.id.toString())))
                    .andExpect(jsonPath("\$.name", equalTo("Project2")))
                    .andExpect(jsonPath("\$.color", equalTo("#000000")))
                    .andExpect(jsonPath("\$.scheduleStartTime", equalTo("09:00")))
                    .andExpect(jsonPath("\$.scheduleEndTime", equalTo("18:00")))
            projectsRepository.findById(project.id).with {
                assert it.isPresent()
                assert it.get().color == "#000000"
                assert it.get().scheduleStartTime == LocalTime.of(9, 0)
                assert it.get().scheduleEndTime == LocalTime.of(18, 0)
                assert it.get().userId == USER_ID
                assert it.get().name == "Project2"
            }
        cleanup:
            projectsRepository.delete(project)
    }

    def "should not change project when updating with null values"() {
        given:
            def project = projectsRepository.save(ProjectEntity.builder()
                    .name("Project1")
                    .userId(USER_ID)
                    .scheduleStartTime(LocalTime.of(8, 0))
                    .scheduleEndTime(LocalTime.of(16, 0))
                    .color("#FF0000")
                    .build())
            def json = buildCreateProjectBody(null, null, null, null)
        when:
            def response = mockMvc.perform(patch(ProjectsResource.RESOURCE_PATH + "/${project.id.toString()}").contentType(APPLICATION_JSON).content(json).with(userIdJwt()))
        then:
            response.andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(jsonPath("\$.id", equalTo(project.id.toString())))
                    .andExpect(jsonPath("\$.name", equalTo("Project1")))
                    .andExpect(jsonPath("\$.color", equalTo("#FF0000")))
                    .andExpect(jsonPath("\$.scheduleStartTime", equalTo("08:00")))
                    .andExpect(jsonPath("\$.scheduleEndTime", equalTo("16:00")))
            projectsRepository.findById(project.id).with {
                assert it.isPresent()
                assert it.get().color == project.color
                assert it.get().scheduleStartTime == project.scheduleStartTime
                assert it.get().scheduleEndTime == project.scheduleEndTime
                assert it.get().userId == project.userId
                assert it.get().name == project.name
            }
        cleanup:
            projectsRepository.delete(project)
    }

    def "should throw 400 when trying to update project with invalid data"() {
        given:
            def project = projectsRepository.save(ProjectEntity.builder()
                    .name("Project1")
                    .userId(USER_ID)
                    .scheduleStartTime(LocalTime.of(8, 0))
                    .scheduleEndTime(LocalTime.of(16, 0))
                    .color("#FF0000")
                    .build())
            def json = buildCreateProjectBody(name, color, scheduleStartTime, scheduleEndTime)
        when:
            def response = mockMvc.perform(patch(ProjectsResource.RESOURCE_PATH + "/${project.id.toString()}").contentType(APPLICATION_JSON).content(json).with(userIdJwt()))
        then:
            response.andExpect(status().isBadRequest())
                    .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("\$.violations[?(@.field == '${field}')].message", hasItem(violation)))
        cleanup:
            projectsRepository.delete(project)
        where:
            name                                   | color | scheduleStartTime | scheduleEndTime | field               | violation
            ""                                     | null  | null              | null            | "name"              | "must be null or not blank"
            RandomStringUtils.randomAlphabetic(50) | null  | null              | null            | "name"              | "size must be between 1 and 25"
            null                                   | null  | null              | "12:13"         | "scheduleEndTime"   | "must be multiple of '15'"
            null                                   | null  | "16:00"           | "12:00"         | "scheduleStartTime" | "period's start time must be before end time"
            null                                   | null  | null              | "06:00"         | "scheduleStartTime" | "period's start time must be before end time"
    }

    private static GString buildCreateProjectBody(name, color = null, scheduleStartTime = null, scheduleEndTime = null) {
        """
        {
            "name": ${name == null ? null : "\"${name}\""},
            "color": ${color == null ? null : "\"${color}\""},
            "scheduleStartTime": ${scheduleStartTime == null ? null : "\"${scheduleStartTime}\""},
            "scheduleEndTime": ${scheduleEndTime == null ? null : "\"${scheduleEndTime}\""}
        }
        """
    }


}
