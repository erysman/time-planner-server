package com.pw.timeplanner.feature.banned_ranges.repository;


import com.pw.timeplanner.feature.banned_ranges.entity.BannedRangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BannedRangeRepository extends JpaRepository<BannedRangeEntity, UUID> {

    List<BannedRangeEntity> findAllByUserId(String userId);

    Optional<BannedRangeEntity> findByUserIdAndId(String userId, UUID id);
}
