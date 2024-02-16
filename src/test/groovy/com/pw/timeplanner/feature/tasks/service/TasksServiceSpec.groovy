package com.pw.timeplanner.feature.tasks.service

import com.pw.timeplanner.config.TasksProperties
import com.pw.timeplanner.core.entity.JsonNullableMapperImpl
import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO
import com.pw.timeplanner.feature.tasks.api.dto.UpdateTaskDTO
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity
import com.pw.timeplanner.feature.tasks.entity.TaskEntity
import com.pw.timeplanner.feature.tasks.entity.TaskEntityMapper
import com.pw.timeplanner.feature.tasks.entity.TaskEntityMapperImpl
import com.pw.timeplanner.feature.tasks.repository.ProjectsRepository
import com.pw.timeplanner.feature.tasks.repository.TasksRepository
import org.openapitools.jackson.nullable.JsonNullable
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalTime

class TasksServiceSpec extends Specification {
    static final String userId = "user1"
    static final int defaultDurationMin = 60
    TasksProperties properties = Mock({ getDefaultDurationMinutes() >> defaultDurationMin })
    ProjectsRepository projectsRepository = Mock(ProjectsRepository)
    TasksRepository tasksRepository = Mock(TasksRepository)
    TaskEntityMapper taskEntityMapper = new TaskEntityMapperImpl(new JsonNullableMapperImpl())
    TasksOrderService tasksOrderService = Mock(TasksOrderService)
    TasksValidator tasksValidator = Mock(TasksValidator)
    TasksService tasksService = new TasksService(projectsRepository, tasksRepository, taskEntityMapper, tasksOrderService, tasksValidator, properties)

    def "should create task with projectId"() {
        given:
            def projectId = UUID.randomUUID()
            def project = ProjectEntity.builder().id(projectId).name("Project 1").build()
            def taskDto = CreateTaskDTO.builder()
                    .name("Task 1")
                    .projectId(projectId)
                    .startDay(LocalDate.now())
                    .startTime(LocalTime.of(9, 0))
                    .durationMin(60)
                    .build()
        when:
            def createdTaskDTO = tasksService.createTask(userId, taskDto)
        then:
            1 * tasksValidator.validate(taskDto)
            1 * projectsRepository.findOneByUserIdAndId(userId, projectId) >> Optional.of(project)
            1 * tasksRepository.save(_ as TaskEntity) >> { TaskEntity taskEntity -> taskEntity }
            1 * tasksOrderService.setOrderForDayAndProject(userId, _ as TaskEntity)
            assertCreateTaskDTO(createdTaskDTO, taskDto, projectId)
    }

    def "should create task with default project"() {
        given:
            def defaultProjectName = "Default Project"
            def defaultProject = ProjectEntity.builder().id(UUID.randomUUID()).name(defaultProjectName).build()
            properties.getDefaultProjectName() >> defaultProjectName
            def taskDto = CreateTaskDTO.builder()
                    .name("Task 1")
                    .startDay(LocalDate.now())
                    .startTime(LocalTime.of(9, 0))
                    .durationMin(60)
                    .build()
        when:
            def createdTaskDTO = tasksService.createTask(userId, taskDto)
        then:
            1 * tasksValidator.validate(taskDto)
            1 * projectsRepository.findOneByUserIdAndName(userId, defaultProjectName) >> Optional.of(defaultProject)
            1 * tasksRepository.save(_ as TaskEntity) >> { TaskEntity taskEntity -> taskEntity }
            1 * tasksOrderService.setOrderForDayAndProject(userId, _ as TaskEntity)
            assertCreateTaskDTO(createdTaskDTO, taskDto, defaultProject.id)
    }

    private void assertCreateTaskDTO(Optional<TaskDTO> createdTaskDTO, taskDto, projectId) {
        assert createdTaskDTO.isPresent()
        createdTaskDTO.get().with {
            assert it.name == taskDto.name
            assert it.projectId == projectId
            assert it.startDay == taskDto.startDay
            assert it.startTime == taskDto.startTime
            assert it.durationMin == taskDto.durationMin
        }
    }

    def "should delete task"() {
        given:
            def taskId = UUID.randomUUID()
            def taskEntity = TaskEntity.builder().id(taskId).userId(userId).build()
        when:
            def isDeleted = tasksService.deleteTask(userId, taskId)
        then:
            1 * tasksRepository.findOneByUserIdAndId(userId, taskId) >> Optional.of(taskEntity)
            1 * tasksRepository.delete(taskEntity)
            1 * tasksOrderService.unsetOrderForDayAndProject(userId, taskEntity)
            isDeleted
    }

    def "should not delete task when it's not found"() {
        given:
            def taskId = UUID.randomUUID()
        when:
            def isDeleted = tasksService.deleteTask(userId, taskId)
        then:
            1 * tasksRepository.findOneByUserIdAndId(userId, taskId) >> Optional.empty()
            0 * tasksRepository.delete(_)
            0 * tasksOrderService.unsetOrderForDayAndProject(userId, _)
            !isDeleted
    }

    def "should update task's name"() {
        given:
            def taskId = UUID.randomUUID()
            def project = ProjectEntity.builder().id(UUID.randomUUID()).name("Project 1").build()
            def taskEntity = TaskEntity.builder().id(taskId).userId(userId).project(project).build()
            def updateTaskDto = UpdateTaskDTO.builder()
                    .name("Task 1")
                    .build()
        when:
            def updatedTaskDTO = tasksService.updateTask(userId, taskId, updateTaskDto)
        then:
            1 * tasksRepository.findAndLockOneByUserIdAndId(userId, taskId) >> Optional.of(taskEntity)
            1 * tasksValidator.validate(updateTaskDto)
            updatedTaskDTO.isPresent()
            updatedTaskDTO.get().name == updateTaskDto.name
    }

    def "should not update task when it's not found"() {
        given:
            def taskId = UUID.randomUUID()
            def updateTaskDto = UpdateTaskDTO.builder()
                    .name("Task 1")
                    .build()
        when:
            def updatedTaskDTO = tasksService.updateTask(userId, taskId, updateTaskDto)
        then:
            1 * tasksRepository.findAndLockOneByUserIdAndId(userId, taskId) >> Optional.empty()
            0 * tasksValidator.validate(updateTaskDto)
            updatedTaskDTO.isEmpty()
    }

    def "should update task's project"() {
        given:
            def taskId = UUID.randomUUID()
            def newProjectId = UUID.randomUUID()
            def newProject = ProjectEntity.builder().id(newProjectId).name("Project 2").build()
            def project = ProjectEntity.builder().id(UUID.randomUUID()).name("Project 1").build()
            def taskEntity = TaskEntity.builder().id(taskId).userId(userId).project(project).build()
            def updateTaskDto = UpdateTaskDTO.builder()
                    .projectId(newProjectId)
                    .build()
        when:
            def updatedTaskDTO = tasksService.updateTask(userId, taskId, updateTaskDto)
        then:
            1 * tasksRepository.findAndLockOneByUserIdAndId(userId, taskId) >> Optional.of(taskEntity)
            1 * tasksValidator.validate(updateTaskDto)
            1 * projectsRepository.findOneByUserIdAndId(userId, newProjectId) >> Optional.of(newProject)
            updatedTaskDTO.isPresent()
            taskEntity.project == newProject
    }

    def "should update task's startDay"() {
        given:
            def taskId = UUID.randomUUID()
            def project = ProjectEntity.builder().id(UUID.randomUUID()).name("Project 1").build()
            def taskEntity = TaskEntity.builder().id(taskId).userId(userId).project(project).build()
            def newStartDay = JsonNullable.of(LocalDate.now().plusDays(1))
            def updateTaskDto = UpdateTaskDTO.builder()
                    .startDay(newStartDay)
                    .build()
        when:
            def updatedTaskDTO = tasksService.updateTask(userId, taskId, updateTaskDto)
        then:
            1 * tasksRepository.findAndLockOneByUserIdAndId(userId, taskId) >> Optional.of(taskEntity)
            1 * tasksValidator.validate(updateTaskDto)
            1 * tasksOrderService.updateDayOrder(userId, taskEntity, newStartDay, _)
            updatedTaskDTO.isPresent()
            taskEntity.with {
                it.startDay == updateTaskDto.startDay.get()
                it.autoScheduled == false
            }
    }

    def "should update task's startTime"() {
        given:
            def taskId = UUID.randomUUID()
            def project = ProjectEntity.builder().id(UUID.randomUUID()).name("Project 1").build()
            def taskEntity = TaskEntity.builder().id(taskId).userId(userId).project(project).build()
            def newStartTime = JsonNullable.of(LocalTime.of(10, 0))
            def updateTaskDto = UpdateTaskDTO.builder()
                    .startTime(newStartTime)
                    .build()
        when:
            def updatedTaskDTO = tasksService.updateTask(userId, taskId, updateTaskDto)
        then:
            1 * tasksRepository.findAndLockOneByUserIdAndId(userId, taskId) >> Optional.of(taskEntity)
            1 * tasksValidator.validate(updateTaskDto)
            1 * tasksOrderService.updateDayOrder(userId, taskEntity, _, newStartTime)
            updatedTaskDTO.isPresent()
            taskEntity.with {
                it.startTime == updateTaskDto.startTime.get()
                it.autoScheduled == false
            }
    }


}
