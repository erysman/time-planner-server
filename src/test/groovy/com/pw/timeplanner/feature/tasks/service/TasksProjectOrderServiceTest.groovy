package com.pw.timeplanner.feature.tasks.service

import com.pw.timeplanner.feature.tasks.entity.ProjectEntity
import com.pw.timeplanner.feature.tasks.entity.TaskEntity
import com.pw.timeplanner.feature.tasks.repository.TasksRepository
import com.pw.timeplanner.feature.tasks.service.exceptions.ListOrderException
import spock.lang.Specification

class TasksProjectOrderServiceTest extends Specification {

    TasksRepository tasksRepository = Mock(TasksRepository)
    TasksProjectOrderService service = new TasksProjectOrderService(tasksRepository)

    final static userId = "userId"

    def "should change order of tasks"() {
        given:
            def project = ProjectEntity.builder().id(UUID.randomUUID()).name("Project 1").userId(userId).build()
            def task0 = TaskEntity.builder().id(UUID.randomUUID()).projectOrder(0).project(project).build()
            def task1 = TaskEntity.builder().id(UUID.randomUUID()).projectOrder(1).project(project).build()
            def task2 = TaskEntity.builder().id(UUID.randomUUID()).projectOrder(2).project(project).build()
            def tasks = [task0, task1, task2]
            def newOrder = [task1.id, task2.id, task0.id]
        when:
            def returnedOrder = service.reorder(userId, project, newOrder)
        then:
            1 * tasksRepository.findAndLockTasksWithProjectOrder(userId, project) >> tasks
            task1.projectOrder == 0
            task2.projectOrder == 1
            task0.projectOrder == 2
            returnedOrder == newOrder
    }

    def "should throw ListOrderException when given order contains wrong ids"() {
        given:
            def project = ProjectEntity.builder().id(UUID.randomUUID()).name("Project 1").userId(userId).build()
            def task0 = TaskEntity.builder().id(taskIds[0]).projectOrder(0).project(project).build()
            def task1 = TaskEntity.builder().id(taskIds[1]).projectOrder(1).project(project).build()
            def task2 = TaskEntity.builder().id(taskIds[2]).projectOrder(2).project(project).build()
            def tasks = [task0, task1, task2]
        when:
            service.reorder(userId, project, newOrder)
        then:
            1 * tasksRepository.findAndLockTasksWithProjectOrder(userId, project) >> tasks
            thrown(ListOrderException)
        where:
            taskIds                                                   | newOrder
            [UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()] | [taskIds[2], taskIds[1], taskIds[0], UUID.randomUUID()]
            [UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()] | [taskIds[2], taskIds[1]]
            [UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()] | [taskIds[2], taskIds[1], taskIds[1], taskIds[0]]
    }

    def "should set order for project"() {
        given:
            def project = ProjectEntity.builder().id(UUID.randomUUID()).name("Project 1").userId(userId).build()
            def taskEntity = TaskEntity.builder().id(UUID.randomUUID()).project(project).userId(userId).build()
        when:
            service.setOrder(userId, taskEntity)
        then:
            1 * tasksRepository.findLastProjectOrder(userId, project) >> Optional.of(maxProjectOrder)
            taskEntity.projectOrder == maxProjectOrder + 1
        where:
            maxProjectOrder << [3, 0, 15, 200]
    }

    def "should set task's project order and shift order in previous project when updating task's project"() {
        given:
            def defaultProject = ProjectEntity.builder().id(UUID.randomUUID()).userId(userId).name("inbox").build()
            def project = ProjectEntity.builder().id(UUID.randomUUID()).userId(userId).name("Project 1").build()
            def task = TaskEntity.builder().id(UUID.randomUUID()).name("Task").project(defaultProject).projectOrder(0).build()
            tasksRepository.findLastProjectOrder(userId, project) >> Optional.of(8)
        when:
            service.updateOrder(userId, task, project)
        then:
            task.projectOrder == 9
            1 * tasksRepository.shiftProjectOrderOfAllTasksAfterDeletedOne(userId, defaultProject, 0)
    }

    def "should not change task's project order when updating task's project to previous one"() {
        given:
            def defaultProject = ProjectEntity.builder().id(UUID.randomUUID()).userId(userId).name("inbox").build()
            def task = TaskEntity.builder().id(UUID.randomUUID()).name("Task").project(defaultProject).projectOrder(8).build()
            tasksRepository.findLastProjectOrder(userId, defaultProject) >> Optional.of(8)
        when:
            service.updateOrder(userId, task, defaultProject)
        then:
            task.projectOrder == 8
            0 * tasksRepository.shiftProjectOrderOfAllTasksAfterDeletedOne(userId, _, _)
    }
}
