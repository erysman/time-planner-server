package com.pw.timeplanner.feature.tasks.repository;


import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    @Query("select t from TaskEntity t where t.userId = :userId and t.startDay = :startDay")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<TaskEntity> findAllByUserIdAndStartDay(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t.id from TaskEntity t where t.userId = :userId and t.startDay = :startDay and t.dayOrder is not null order by t.dayOrder")
    List<UUID> findTaskIdsOrderedByDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("select t.id from TaskEntity t where t.userId = :userId and t.startDay = :startDay and t.dayOrder is not null ")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Set<UUID> findTaskIdsWithDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("update TaskEntity t SET t.dayOrder = :dayOrder where t.id = :id and t.userId = :userId")
    @Modifying
    int updateDayOrder(@Param("userId") String userId, @Param("id") UUID id, @Param("dayOrder") Integer dayOrder);

    @Query("select max(t.dayOrder) from TaskEntity t where t.userId = :userId and t.startDay = :startDay group by t.startDay")
    Optional<Integer> findLastDayOrder(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

    @Query("update TaskEntity t set t.dayOrder = t.dayOrder-1 where t.userId = :userId and t.startDay = :startDay and t.dayOrder > :deletedPosition")
    @Modifying
    int shiftOrderOfAllTasksAfterDeletedOne(@Param("userId") String userId, @Param("startDay") LocalDate startDay, @Param("deletedPosition") Integer deletedPosition);

    @Query("select count(*) from TaskEntity t where t.userId = :userId and t.startDay = :startDay and t.autoScheduled = true")
    int countAutoScheduledTasks(@Param("userId") String userId, @Param("startDay") LocalDate startDay);
}
