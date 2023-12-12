package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import com.pw.timeplanner.feature.tasks.repository.TasksRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@AllArgsConstructor
@Profile("local")
@Slf4j
public class LocalDataInitializer {

    private final TasksRepository tasksRepository;

    @PostConstruct
    private void initData() {
        String userId = "oNK797T3SAfA0Z4nvy8oFWR7WOi2";
        LocalDate now = LocalDate.now();

        if(!tasksRepository.findAllByUserIdAndStartDay(userId, now).isEmpty()) {
            return;
        }
        log.info("Initializing db with mocked entities for day: {}", now);
        TaskEntity e1 = TaskEntity.builder().userId(userId).name("Sprzątanie").startDay(LocalDate.now()).dayOrder(1).build();
        TaskEntity e4 = TaskEntity.builder().userId(userId).name("Nauka").startDay(LocalDate.now()).dayOrder(2).build();
        TaskEntity e5 = TaskEntity.builder().userId(userId).name("Bieganie").startDay(LocalDate.now()).dayOrder(0).build();
        TaskEntity e2 = TaskEntity.builder().userId(userId).name("Gotowanie").startDay(LocalDate.now()).startTime(LocalTime.of(9, 0, 0)).durationMin(60).build();
        TaskEntity e3 = TaskEntity.builder().userId(userId).name("Gotowanie2").startDay(LocalDate.now()).startTime(LocalTime.of(10, 15, 0)).durationMin(120).build();
        tasksRepository.save(e5);
        tasksRepository.save(e1);
        tasksRepository.save(e2);
        tasksRepository.save(e3);
        tasksRepository.save(e4);
    }

}
