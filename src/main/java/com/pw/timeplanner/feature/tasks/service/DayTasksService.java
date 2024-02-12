package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.feature.tasks.service.exceptions.DataConflictException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

//@Service
//@AllArgsConstructor
//@Slf4j
//@Transactional
//public class DayTasksService {
//
//    private final TasksOrderService tasksOrderService;
//
//    public List<UUID> getTasksOrder(String userId, LocalDate day) {
//        return tasksOrderService.getTasksOrderForDay(userId, day);
//    }
//
//    public List<UUID> updateTasksOrder(String userId, LocalDate day, List<UUID> tasksOrder) throws DataConflictException {
//        return tasksOrderService.reorderTasksForDay(userId, day, tasksOrder);
//    }
//
//
//}
