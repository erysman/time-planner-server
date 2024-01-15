package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.feature.banned_ranges.repository.BannedRangeRepository;
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
import java.util.Optional;

@Component
@AllArgsConstructor
@Profile("local")
@Slf4j
public class LocalDataInitializer {

    private final TasksProperties properties;
    private final TasksRepository tasksRepository;
    private final ProjectsRepository projectsRepository;
    private final BannedRangeRepository bannedRangeRepository;
    private PlatformTransactionManager transactionManager;


    @PostConstruct
    public void initData() {
        String userId = "oNK797T3SAfA0Z4nvy8oFWR7WOi2";
        LocalDate now = LocalDate.now();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(status -> {
            Optional<ProjectEntity> defaultProject = projectsRepository.findOneByUserIdAndName(userId,
                    properties.getDefaultProjectName());
            ProjectEntity p1 = defaultProject.orElseGet(() -> {
                log.info("Initializing default project for user: {}", userId);
                ProjectEntity newProject = ProjectEntity.builder()
                        .name(properties.getDefaultProjectName())
                        .userId(userId)
                        .color("blue")
                        .scheduleStartTime(LocalTime.of(0, 0, 0))
                        .scheduleEndTime(LocalTime.of(23, 59, 59))
                        .build();
                return projectsRepository.save(newProject);
            });
            if (!tasksRepository.findAllByUserIdAndStartDay(userId, now)
                    .isEmpty()) {
                return "";
            }
            log.info("Initializing db with mocked entities for day: {}", now);
            TaskEntity e1 = TaskEntity.builder()
                    .userId(userId)
                    .name("Sprzątanie")
                    .startDay(LocalDate.now())
                    .dayOrder(1)
                    .project(p1)
                    .build();
            TaskEntity e4 = TaskEntity.builder()
                    .userId(userId)
                    .name("Nauka")
                    .startDay(LocalDate.now())
                    .dayOrder(2)
                    .isImportant(true)
                    .project(p1)
                    .build();
            TaskEntity e5 = TaskEntity.builder()
                    .userId(userId)
                    .name("Bieganie")
                    .startDay(LocalDate.now())
                    .dayOrder(0)
                    .isImportant(true)
                    .project(p1)
                    .build();
            TaskEntity e2 = TaskEntity.builder()
                    .userId(userId)
                    .name("Gotowanie")
                    .startDay(LocalDate.now())
                    .startTime(LocalTime.of(9, 0, 0))
                    .durationMin(60)
                    .project(p1)
                    .build();
            TaskEntity e3 = TaskEntity.builder()
                    .userId(userId)
                    .name("Gotowanie2")
                    .startDay(LocalDate.now())
                    .startTime(LocalTime.of(10, 15, 0))
                    .durationMin(120)
                    .project(p1)
                    .build();
            tasksRepository.save(e1);
            tasksRepository.save(e2);
            tasksRepository.save(e3);
            tasksRepository.save(e4);
            tasksRepository.save(e5);
            return "";
        });
        transactionTemplate.execute(status -> {
//            BannedRangeEntity r1 = BannedRangeEntity.builder()
//                    .startTime(LocalTime.MIN)
//                    .endTime(LocalTime.of(8,0,0))
//                    .build();
//            BannedRangeEntity r2 = BannedRangeEntity.builder()
//                    .startTime(LocalTime.of(22,0,0))
//                    .endTime(LocalTime.MAX)
//                    .build();
//            bannedRangeRepository.save(r1);
//            bannedRangeRepository.save(r2);

            return "";
        });

    }

}
