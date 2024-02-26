package com.pw.timeplanner.feature.tasks.service

import com.pw.timeplanner.feature.projects.Project
import com.pw.timeplanner.feature.tasks.Task
import com.pw.timeplanner.feature.tasks.TasksProjectOrderService
import com.pw.timeplanner.feature.tasks.TasksRepository
import com.pw.timeplanner.feature.tasks.exceptions.ListOrderException
import spock.lang.Specification

class TasksProjectOrderServiceTest extends Specification {

    TasksRepository tasksRepository = Mock(TasksRepository)
    TasksProjectOrderService service = new TasksProjectOrderService(tasksRepository)

    final static userId = "userId"

    def "should change order of tasks"() {
        given:
            def project = Project.builder().id(UUID.randomUUID()).name("Project 1").userId(userId).build()
            def task0 = Task.builder().id(UUID.randomUUID()).projectOrder(0).projectId(project).build()
            def task1 = Task.builder().id(UUID.randomUUID()).projectOrder(1).projectId(project).build()
            def task2 = Task.builder().id(UUID.randomUUID()).projectOrder(2).projectId(project).build()
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
            def project = Project.builder().id(UUID.randomUUID()).name("Project 1").userId(userId).build()
            def task0 = Task.builder().id(taskIds[0]).projectOrder(0).projectId(project).build()
            def task1 = Task.builder().id(taskIds[1]).projectOrder(1).projectId(project).build()
            def task2 = Task.builder().id(taskIds[2]).projectOrder(2).projectId(project).build()
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
            def project = Project.builder().id(UUID.randomUUID()).name("Project 1").userId(userId).build()
            def taskEntity = Task.builder().id(UUID.randomUUID()).projectId(project).userId(userId).build()
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
            def defaultProject = Project.builder().id(UUID.randomUUID()).userId(userId).name("inbox").build()
            def project = Project.builder().id(UUID.randomUUID()).userId(userId).name("Project 1").build()
            def task = Task.builder().id(UUID.randomUUID()).name("Task").projectId(defaultProject).projectOrder(0).build()
            tasksRepository.findLastProjectOrder(userId, project) >> Optional.of(8)
        when:
            service.updateOrder(userId, task, project)
        then:
            task.projectOrder == 9
            1 * tasksRepository.shiftProjectOrderOfAllTasksAfterDeletedOne(userId, defaultProject, 0)
    }

    def "should not change task's project order when updating task's project to previous one"() {
        given:
            def defaultProject = Project.builder().id(UUID.randomUUID()).userId(userId).name("inbox").build()
            def task = Task.builder().id(UUID.randomUUID()).name("Task").projectId(defaultProject).projectOrder(8).build()
            tasksRepository.findLastProjectOrder(userId, defaultProject) >> Optional.of(8)
        when:
            service.updateOrder(userId, task, defaultProject)
        then:
            task.projectOrder == 8
            0 * tasksRepository.shiftProjectOrderOfAllTasksAfterDeletedOne(userId, _, _)
    }
}
