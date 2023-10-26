package com.pw.timeplanner.feature.tasks.repository;


import com.pw.timeplanner.feature.tasks.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TasksRepository extends JpaRepository<TaskEntity, UUID> {

    List<TaskEntity> findAllByUserId(String userId);

    Optional<TaskEntity> findOneByUserIdAndId(String userId, UUID taskId);

    @Query("select t from TaskEntity t where t.userId = :userId and t.startDate = :startDate")
    List<TaskEntity> findAllByUserIdAndStartDate(@Param("userId") String userId, @Param("startDate") LocalDate startDate);


}
