package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.feature.tasks.entity.Priority;
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.repository.ProjectsRepository;
import com.pw.timeplanner.feature.tasks.repository.TasksRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@AllArgsConstructor
@Profile("local")
@Slf4j
public class LocalDataInitializer {

    private final TasksRepository tasksRepository;
    private final ProjectsRepository projectsRepository;
    private PlatformTransactionManager transactionManager;


    @PostConstruct
    public void initData() {
        String userId = "oNK797T3SAfA0Z4nvy8oFWR7WOi2";
        LocalDate now = LocalDate.now();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(status -> {
            if (!tasksRepository.findAllByUserIdAndStartDay(userId, now)
                    .isEmpty()) {
                return "";
            }
            log.info("Initializing db with mocked entities for day: {}", now);
            ProjectEntity p1 = ProjectEntity.builder()
                    .name("inbox")
                    .scheduleStartTime(LocalTime.of(0, 0, 0))
                    .scheduleEndTime(LocalTime.of(23, 59, 59))
                    .build();
            ProjectEntity savedProject = projectsRepository.save(p1);
            TaskEntity e1 = TaskEntity.builder()
                    .userId(userId)
                    .name("Sprzątanie")
                    .startDay(LocalDate.now())
                    .dayOrder(1)
                    .priority(Priority.NORMAL)
                    .project(savedProject)
                    .build();
            TaskEntity e4 = TaskEntity.builder()
                    .userId(userId)
                    .name("Nauka")
                    .startDay(LocalDate.now())
                    .dayOrder(2)
                    .priority(Priority.IMPORTANT)
                    .project(savedProject)
                    .build();
            TaskEntity e5 = TaskEntity.builder()
                    .userId(userId)
                    .name("Bieganie")
                    .startDay(LocalDate.now())
                    .dayOrder(0)
                    .priority(Priority.IMPORTANT)
                    .project(savedProject)
                    .build();
            TaskEntity e2 = TaskEntity.builder()
                    .userId(userId)
                    .name("Gotowanie")
                    .startDay(LocalDate.now())
                    .startTime(LocalTime.of(9, 0, 0))
                    .durationMin(60)
                    .priority(Priority.NORMAL)
                    .project(savedProject)
                    .build();
            TaskEntity e3 = TaskEntity.builder()
                    .userId(userId)
                    .name("Gotowanie2")
                    .startDay(LocalDate.now())
                    .startTime(LocalTime.of(10, 15, 0))
                    .durationMin(120)
                    .priority(Priority.NORMAL)
                    .project(savedProject)
                    .build();
            tasksRepository.save(e1);
            tasksRepository.save(e2);
            tasksRepository.save(e3);
            tasksRepository.save(e4);
            tasksRepository.save(e5);
//            e1.setProject(savedProject);
//            e2.setProject(savedProject);
//            e3.setProject(savedProject);
//            e4.setProject(savedProject);
//            e5.setProject(savedProject);
            return "";
        });

    }

}
