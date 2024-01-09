package com.pw.timeplanner.feature.tasks.repository;


import com.pw.timeplanner.feature.tasks.entity.BannedRangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BannedRangeRepository extends JpaRepository<BannedRangeEntity, UUID> {

//    @Query("select t from TaskEntity t where t.userId = :userId and t.startDay = :startDay")
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    List<TaskEntity> findAllByUserIdAndStartDay(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

//    List<BannedRangeEntity> findAll();
}
