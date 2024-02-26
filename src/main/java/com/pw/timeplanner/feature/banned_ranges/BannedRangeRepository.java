package com.pw.timeplanner.feature.banned_ranges;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface BannedRangeRepository extends JpaRepository<BannedRangeEntity, UUID> {

    List<BannedRangeEntity> findAllByUserId(String userId);

    Optional<BannedRangeEntity> findByUserIdAndId(String userId, UUID id);
}
