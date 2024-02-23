package com.pw.timeplanner.feature.tasks.repository;


import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
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
public interface TasksRepository extends JpaRepository<TaskEntity, UUID> {

    Optional<TaskEntity> findOneByUserIdAndId(String userId, UUID taskId);

    @Query("SELECT t FROM TaskEntity t JOIN FETCH t.project WHERE t.userId = :userId AND t.startDay = :day")
    Set<TaskEntity> findAllWithProjectByUserIdAndStartDay(@Param("userId") String userId, @Param("day") LocalDate day);

    @Query("select t from TaskEntity t where t.userId = :userId and t.id = :taskId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TaskEntity> findAndLockOneByUserIdAndId(@Param("userId") String userId, @Param("taskId") UUID taskId);

    @Query("select t from TaskEntity t where t.userId = :userId and t.startDay = :startDay")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TaskEntity> findAndLockAllByUserIdAndStartDay(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t from TaskEntity t where t.userId = :userId and t.startDay = :startDay and t.startTime is null")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TaskEntity> findAndLockAllByUserIdAndStartDayAndStartTimeIsNull(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("SELECT t FROM TaskEntity t JOIN FETCH t.project WHERE t.userId = :userId AND t.startDay = :day")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TaskEntity> findAndLockAllByUserIdAndStartDayWithProjects(@Param("userId") String userId, @Param("day") LocalDate day);

    @QueryHints(@QueryHint(name = AvailableHints.HINT_CACHEABLE, value = "true"))
//    @Query("select t from TaskEntity t where t.userId = :userId and t.startDay = :startDay")
    List<TaskEntity> findAllByUserIdAndStartDay(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t from TaskEntity t where t.userId = :userId and t.startDay = :startDay and t.dayOrder is not null order by t.dayOrder")
    List<TaskEntity> findTasksWithDayOrderOrderedByDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t.id from TaskEntity t where t.userId = :userId and t.startDay = :startDay and t.dayOrder is not null order by t.dayOrder")
    List<UUID> findTaskIdsOrderedByDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t.id from TaskEntity t where t.userId = :userId and t.project = :project and t.projectOrder is not null order by t.projectOrder")
    List<UUID> findTaskIdsOrderedByProject(@Param("userId") String userId, @Param("project") ProjectEntity project);

    @Query("select t.id from TaskEntity t where t.userId = :userId and t.startDay = :startDay and t.dayOrder is not null ")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Set<UUID> findAndLockTaskIdsWithDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t from TaskEntity t where t.userId = :userId and t.startDay = :startDay and t.dayOrder is not null ")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Set<TaskEntity> findAndLockTasksWithDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t from TaskEntity t where t.userId = :userId and t.project = :project and t.projectOrder is not null ")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Set<TaskEntity> findAndLockTasksWithProjectOrder(@Param("userId") String userId, @Param("project") ProjectEntity project);

    @Query("select max(t.dayOrder) from TaskEntity t where t.userId = :userId and t.startDay = :startDay group by t.startDay")
    Optional<Integer> findLastDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select max(t.projectOrder) from TaskEntity t where t.userId = :userId and t.project = :project group by t.project")
    Optional<Integer> findLastProjectOrder(@Param("userId") String userId, @Param("project") ProjectEntity project);

    @Query("update TaskEntity t set t.dayOrder = t.dayOrder-1 where t.userId = :userId and t.startDay = :startDay and t.dayOrder > :deletedPosition")
    @Modifying
    void shiftDayOrderOfAllTasksAfterDeletedOne(@Param("userId") String userId, @Param("startDay") LocalDate startDay, @Param("deletedPosition") Integer deletedPosition);

    @Query("update TaskEntity t set t.projectOrder = t.projectOrder-1 where t.userId = :userId and t.project = :project and t.projectOrder > :deletedPosition")
    @Modifying
    void shiftProjectOrderOfAllTasksAfterDeletedOne(@Param("userId") String userId, @Param("project") ProjectEntity project, @Param("deletedPosition") Integer deletedPosition);


    @Query("select count(*) from TaskEntity t where t.userId = :userId and t.startDay = :startDay and t.autoScheduled = true")
    int countAutoScheduledTasks(@Param("userId") String userId, @Param("startDay") LocalDate startDay);
}
