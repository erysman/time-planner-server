package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.feature.tasks.api.projectDto.CreateProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.ProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.UpdateProjectDTO;
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.entity.ProjectEntityMapper;
import com.pw.timeplanner.feature.tasks.repository.ProjectsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectsRepository projectsRepository;
    private final ProjectEntityMapper mapper;

    public List<ProjectDTO> getProjects(String userId) {
        return projectsRepository.findAllByUserId(userId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    public Optional<ProjectDTO> getProject(String userId, UUID projectId) {
        Optional<ProjectEntity> projectEntity = projectsRepository.findOneByUserIdAndId(userId, projectId);
        return projectEntity.map(mapper::toDTO);
    }

    public void deleteProject(String userId, UUID projectId) {
        projectsRepository.deleteByUserIdAndId(userId, projectId);
    }

    public Optional<ProjectDTO> updateProject(String userId, UUID projectId, UpdateProjectDTO updateProjectDTO) {
        Optional<ProjectEntity> projectEntity = projectsRepository.findOneByUserIdAndId(userId, projectId);
        if(projectEntity.isEmpty()) return Optional.empty();
        ProjectEntity project = projectEntity.get();
        mapper.update(updateProjectDTO, project);
        return Optional.of(mapper.toDTO(project));
    }

    public ProjectDTO createProject(String userId, CreateProjectDTO createProjectDTO) {
        ProjectEntity entity = mapper.createEntity(createProjectDTO);
        entity.setUserId(userId);
        return mapper.toDTO(projectsRepository.save(entity));
    }
}
