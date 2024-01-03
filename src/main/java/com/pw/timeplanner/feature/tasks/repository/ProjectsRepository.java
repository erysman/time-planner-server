package com.pw.timeplanner.feature.tasks.repository;


import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ProjectsRepository extends JpaRepository<ProjectEntity, UUID> {

    Optional<ProjectEntity> findOneByUserIdAndId(String userId, UUID projectId);

    Optional<ProjectEntity> findOneByUserIdAndName(String userId, String name);

    Set<ProjectEntity> findAllByUserId(String userId);

    void deleteByUserIdAndId(String userId, UUID projectId);

}
