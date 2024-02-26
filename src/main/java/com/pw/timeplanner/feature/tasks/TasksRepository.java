package com.pw.timeplanner.feature.tasks;


import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.AvailableHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
interface TasksRepository extends JpaRepository<Task, UUID> {

    Optional<Task> findOneByUserIdAndId(String userId, UUID taskId);

    Set<Task> findAllByUserIdAndIdIsIn(String userId, Set<UUID> taskIds);

    Set<Task> findAllByUserIdAndProjectId(String userId, UUID projectId);

    @Query("SELECT t FROM Task t JOIN FETCH t.project WHERE t.userId = :userId AND t.startDay = :day")
    Set<Task> findAllWithProjectByUserIdAndStartDay(@Param("userId") String userId, @Param("day") LocalDate day);

    @Query("select t from Task t where t.userId = :userId and t.id = :taskId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Task> findAndLockOneByUserIdAndId(@Param("userId") String userId, @Param("taskId") UUID taskId);

    @Query("select t from Task t where t.userId = :userId and t.startDay = :startDay")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Task> findAndLockAllByUserIdAndStartDay(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t from Task t where t.userId = :userId and t.startDay = :startDay and t.startTime is null")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Task> findAndLockAllByUserIdAndStartDayAndStartTimeIsNull(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("SELECT t FROM Task t JOIN FETCH t.project WHERE t.userId = :userId AND t.startDay = :day")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Task> findAndLockAllByUserIdAndStartDayWithProjects(@Param("userId") String userId, @Param("day") LocalDate day);

    @QueryHints(@QueryHint(name = AvailableHints.HINT_CACHEABLE, value = "true"))
//    @Query("select t from TaskEntity t where t.userId = :userId and t.startDay = :startDay")
    List<Task> findAllByUserIdAndStartDay(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t from Task t where t.userId = :userId and t.startDay = :startDay and t.dayOrder is not null order by t.dayOrder")
    List<Task> findTasksWithDayOrderOrderedByDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t.id from Task t where t.userId = :userId and t.startDay = :startDay and t.dayOrder is not null order by t.dayOrder")
    List<UUID> findTaskIdsOrderedByDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t.id from Task t where t.userId = :userId and t.startDay = :startDay and t.dayOrder is not null ")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Set<UUID> findAndLockTaskIdsWithDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t from Task t where t.userId = :userId and t.startDay = :startDay and t.dayOrder is not null ")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Set<Task> findAndLockTasksWithDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select max(t.dayOrder) from Task t where t.userId = :userId and t.startDay = :startDay group by t.startDay")
    Optional<Integer> findLastDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("update Task t set t.dayOrder = t.dayOrder-1 where t.userId = :userId and t.startDay = :startDay and t.dayOrder > :deletedPosition")
    @Modifying
    void shiftDayOrderOfAllTasksAfterDeletedOne(@Param("userId") String userId, @Param("startDay") LocalDate startDay, @Param("deletedPosition") Integer deletedPosition);


    @Query("select max(t.projectOrder) from Task t where t.userId = :userId and t.projectId = :projectId group by t.projectId")
    Optional<Integer> findLastProjectOrder(@Param("userId") String userId, @Param("projectId") UUID projectId);

    @Query("select t from Task t where t.userId = :userId and t.projectId = :projectId and t.projectOrder is not null ")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Set<Task> findAndLockTasksWithProjectOrder(@Param("userId") String userId, @Param("projectId") UUID projectId);

    @Query("select t.id from Task t where t.userId = :userId and t.projectId = :projectId and t.projectOrder is not null order by t.projectOrder")
    List<UUID> findTaskIdsOrderedByProject(@Param("userId") String userId, @Param("projectId") UUID projectId);

    @Query("update Task t set t.projectOrder = t.projectOrder-1 where t.userId = :userId and t.projectId = :projectId and t.projectOrder > :deletedPosition")
    @Modifying
    void shiftProjectOrderOfAllTasksAfterDeletedOne(@Param("userId") String userId, @Param("projectId") UUID projectId, @Param("deletedPosition") Integer deletedPosition);


    @Query("select count(*) from Task t where t.userId = :userId and t.startDay = :startDay and t.autoScheduled = true")
    int countAutoScheduledTasks(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

}
