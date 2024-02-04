package com.pw.timeplanner.feature.tasks.service

import com.pw.timeplanner.config.TasksProperties
import com.pw.timeplanner.feature.tasks.api.projectDto.ProjectDTO
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity
import com.pw.timeplanner.feature.tasks.entity.TaskEntityMapper
import spock.lang.Specification
import com.pw.timeplanner.feature.tasks.service.ProjectService
import com.pw.timeplanner.feature.tasks.repository.ProjectsRepository
import com.pw.timeplanner.feature.tasks.entity.ProjectEntityMapper

import java.time.LocalTime


class ProjectServiceSpec extends Specification {

    def "validate getProjects returns correct number of projects for a user"() {
        given: "A ProjectService with mocked dependencies"
            def projectsRepository = Mock(ProjectsRepository)
            def projectEntityMapper = Mock(ProjectEntityMapper)
            def taskEntityMapper = Mock(TaskEntityMapper)
            def tasksProperties = Mock(TasksProperties)
            def projectService = new ProjectService(projectsRepository, projectEntityMapper, taskEntityMapper, tasksProperties);

        and: "A user with ID '$userId' and $numberOfProjects projects in the system"
            def mockProjects = (0..<numberOfProjects).collect { index ->
                ProjectDTO.builder()
                        .id(UUID.randomUUID())
                        .name("Project $index")
                        .scheduleStartTime(LocalTime.of(9, 0))
                        .scheduleEndTime(LocalTime.of(17, 0))
                        .color("Color $index")
                        .build()
            }
            projectsRepository.findAllByUserId(userId) >> mockProjects.collect { new ProjectEntity() }
            mockProjects.each { projectEntityMapper.toDTO(_) >> it }

        when: "getProjects is called with '$userId'"
            def result = projectService.getProjects(userId)

        then: "The method returns $numberOfProjects ProjectDTO objects"
            result.size() == numberOfProjects
            result.every { it instanceof ProjectDTO }

        where:
            userId        | numberOfProjects
            'user1'       | 0
            'user2'       | 3
    }
}