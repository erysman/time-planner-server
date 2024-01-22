package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.CreateProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.ProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.UpdateProjectDTO;
import com.pw.timeplanner.feature.tasks.entity.ProjectEntity;
import com.pw.timeplanner.feature.tasks.entity.ProjectEntityMapper;
import com.pw.timeplanner.feature.tasks.entity.TaskEntityMapper;
import com.pw.timeplanner.feature.tasks.repository.ProjectsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.pw.timeplanner.config.Constants.LOCAL_TIME_MAX;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectsRepository projectsRepository;
    private final ProjectEntityMapper mapper;
    private final TaskEntityMapper taskMapper;
    private final TasksProperties properties;

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

    public Optional<ProjectDTO> getDefaultProject(String userId) {
        Optional<ProjectEntity> projectEntity = projectsRepository.findOneByUserIdAndName(userId,
                properties.getDefaultProjectName());
        return projectEntity.map(mapper::toDTO);
    }

    public ProjectDTO createDefaultProject(String userId) {
        Optional<ProjectDTO> existingDefaultProject = this.getDefaultProject(userId);
        if (existingDefaultProject.isPresent()) {
            return existingDefaultProject.get();
        }
        ProjectEntity defaultProject = ProjectEntity.builder()
                .name(properties.getDefaultProjectName())
                .userId(userId)
                .color(properties.getDefaultProjectColor())
                .scheduleStartTime(LocalTime.MIN)
                .scheduleEndTime(LOCAL_TIME_MAX)
                .build();
        return mapper.toDTO(projectsRepository.save(defaultProject));
    }

    public void deleteProject(String userId, UUID projectId) {
        Optional<ProjectEntity> projectEntity = projectsRepository.findOneByUserIdAndId(userId, projectId);
        Optional<Boolean> existsAndIsDefaultProject = projectEntity.map(p -> p.getName()
                .equals(properties.getDefaultProjectName()));
        existsAndIsDefaultProject.ifPresent(isDefaultProject -> {
            if (isDefaultProject) {
                ProjectEntity copy = new ProjectEntity(projectEntity.get());
                projectEntity.ifPresent(projectsRepository::delete);
                projectsRepository.flush();
                projectsRepository.save(copy);
                return;
            }
            projectEntity.ifPresent(projectsRepository::delete);
        });

    }

    public Optional<ProjectDTO> updateProject(String userId, UUID projectId, UpdateProjectDTO updateProjectDTO) {
        Optional<ProjectEntity> projectEntity = projectsRepository.findOneByUserIdAndId(userId, projectId);
        if (projectEntity.isEmpty()) return Optional.empty();
        ProjectEntity project = projectEntity.get();
        mapper.update(updateProjectDTO, project);
        return Optional.of(mapper.toDTO(project));
    }

    public ProjectDTO createProject(String userId, CreateProjectDTO createProjectDTO) {
        ProjectEntity entity = mapper.createEntity(createProjectDTO);
        entity.setUserId(userId);
        if(entity.getScheduleStartTime() == null) {
            entity.setScheduleStartTime(LocalTime.MIN);
        }
        if(entity.getScheduleEndTime() == null) {
            entity.setScheduleEndTime(LOCAL_TIME_MAX);
        }
        return mapper.toDTO(projectsRepository.save(entity));
    }

    public List<TaskDTO> getProjectTasks(String userId, UUID projectId) {
        Optional<ProjectEntity> projectEntity = projectsRepository.findOneByUserIdAndId(userId, projectId);
        return projectEntity.map(entity -> entity.getTasks()
                        .stream()
                        .map(taskMapper::toDTO)
                        .toList())
                .orElseGet(List::of);
    }
}
