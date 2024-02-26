package com.pw.timeplanner.feature.repository;

import com.pw.timeplanner.feature.projects.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectsTestRepository extends JpaRepository<Project, UUID>{

    Optional<Project> findByUserIdAndName(String userId, String name);

}
