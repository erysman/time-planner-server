package com.pw.timeplanner.feature.banned_ranges.repository;


import com.pw.timeplanner.feature.banned_ranges.entity.BannedRangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BannedRangeRepository extends JpaRepository<BannedRangeEntity, UUID> {

//    @Query("select t from TaskEntity t where t.userId = :userId and t.startDay = :startDay")
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    List<TaskEntity> findAllByUserIdAndStartDay(@Param("userId") String userId, @Param("startDay") LocalDate startDay);

//    List<BannedRangeEntity> findAll();

    List<BannedRangeEntity> findAllByUserId(String userId);

    Optional<BannedRangeEntity> findByUserIdAndId(String userId, UUID id);
}
