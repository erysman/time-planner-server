package com.pw.timeplanner.feature.projects;


import jakarta.persistence.QueryHint;
import org.hibernate.jpa.AvailableHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
interface ProjectsRepository extends JpaRepository<Project, UUID> {

    @QueryHints(@QueryHint(name = AvailableHints.HINT_CACHEABLE, value = "true"))
    Optional<Project> findOneByUserIdAndId(String userId, UUID projectId);

    Optional<Project> findOneByUserIdAndName(String userId, String name);

    @QueryHints(@QueryHint(name = AvailableHints.HINT_CACHEABLE, value = "true"))
    Set<Project> findAllByUserId(String userId);

    Set<Project> findAllByUserIdAndIdIsIn(String userId, List<UUID> projectIds);
}
