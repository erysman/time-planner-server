package com.pw.timeplanner.feature.tasks.repository;


import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.AvailableHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ProjectsRepository extends JpaRepository<ProjectEntity, UUID> {


    @QueryHints(@QueryHint(name = AvailableHints.HINT_CACHEABLE, value = "true"))
    Optional<ProjectEntity> findOneByUserIdAndId(String userId, UUID projectId);

    Optional<ProjectEntity> findOneByUserIdAndName(String userId, String name);

    @QueryHints(@QueryHint(name = AvailableHints.HINT_CACHEABLE, value = "true"))
    Set<ProjectEntity> findAllByUserId(String userId);

    void deleteByUserIdAndId(String userId, UUID projectId);

}
