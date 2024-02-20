package com.pw.timeplanner.feature.tasks.service;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.core.exception.ResourceAlreadyExistsException;
import com.pw.timeplanner.core.exception.ResourceNotFoundException;
import com.pw.timeplanner.feature.tasks.api.ProjectsResource;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.pw.timeplanner.config.Constants.LOCAL_TIME_MAX;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectsRepository projectsRepository;
    private final ProjectEntityMapper mapper;
    private final TaskEntityMapper taskMapper;
    private final TasksProperties properties;

    public List<ProjectDTO> getProjects(String userId) {
        log.info("Getting projects for user: {}", userId);
        return projectsRepository.findAllByUserId(userId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    public ProjectDTO getProject(String userId, UUID projectId) {
        return mapper.toDTO(getProjectEntity(userId, projectId));
    }

    public ProjectEntity getProjectEntity(String userId, UUID projectId) {
        log.info("Getting project with id: {} for user: {}", projectId, userId);
        return projectsRepository.findOneByUserIdAndId(userId, projectId)
                .orElseThrow( () -> new ResourceNotFoundException(ProjectsResource.RESOURCE_PATH, projectId));
    }

    public ProjectDTO getOrCreateDefaultProject(String userId) {
        return mapper.toDTO(getOrCreateDefaultProjectEntity(userId));
    }

    public ProjectEntity getOrCreateDefaultProjectEntity(String userId) {
        return projectsRepository.findOneByUserIdAndName(userId,
                properties.getDefaultProjectName()).or(() -> Optional.of(createDefaultProject(userId))).orElseThrow();
    }

    private ProjectEntity createDefaultProject(String userId) {
        ProjectEntity defaultProject = ProjectEntity.builder()
                .name(properties.getDefaultProjectName())
                .userId(userId)
                .color(properties.getDefaultProjectColor())
                .scheduleStartTime(LocalTime.MIN)
                .scheduleEndTime(LOCAL_TIME_MAX)
                .build();
        log.info("Creating default project named '{}' for user: {}", properties.getDefaultProjectName(), userId);
        return projectsRepository.save(defaultProject);
    }

    @Transactional
    public void deleteProject(String userId, UUID projectId) {
        log.info("Deleting project with id: {} for user: {}", projectId, userId);
        ProjectEntity projectEntity = projectsRepository.findOneByUserIdAndId(userId, projectId)
                .orElseThrow( () -> new ResourceNotFoundException(ProjectsResource.RESOURCE_PATH, projectId));
        boolean existsAndIsDefaultProject = projectEntity.getName()
                .equals(properties.getDefaultProjectName());
        if (existsAndIsDefaultProject) {
            ProjectEntity copy = new ProjectEntity(projectEntity);
            projectsRepository.delete(projectEntity);
            projectsRepository.flush();
            projectsRepository.save(copy);
            return;
        }
        projectsRepository.delete(projectEntity);

    }

    @Transactional
    public ProjectDTO updateProject(String userId, UUID projectId, UpdateProjectDTO updateProjectDTO) {
        log.info("Updating project with id: {} for user: {}", projectId, userId);
        ProjectEntity projectEntity = projectsRepository.findOneByUserIdAndId(userId, projectId)
                .orElseThrow( () -> new ResourceNotFoundException(ProjectsResource.RESOURCE_PATH, projectId));
        mapper.update(updateProjectDTO, projectEntity);
        return mapper.toDTO(projectEntity);
    }

    public ProjectDTO createProject(String userId, CreateProjectDTO createProjectDTO) {
        log.info("Creating project for user: {}", userId);
        ProjectEntity entity = mapper.createEntity(createProjectDTO);
        entity.setUserId(userId);
        if(entity.getScheduleStartTime() == null) {
            entity.setScheduleStartTime(LocalTime.MIN);
        }
        if(entity.getScheduleEndTime() == null) {
            entity.setScheduleEndTime(LOCAL_TIME_MAX);
        }
        try {
            return mapper.toDTO(projectsRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            log.info("Project with name: {} already exists for user: {}", createProjectDTO.getName(), userId);
            throw new ResourceAlreadyExistsException(ProjectsResource.RESOURCE_PATH, "name", createProjectDTO.getName());
        }
    }

    public List<TaskDTO> getProjectTasks(String userId, UUID projectId) {
        log.info("Getting tasks for project with id: {} for user: {}", projectId, userId);
        Optional<ProjectEntity> projectEntity = projectsRepository.findOneByUserIdAndId(userId, projectId);
        return projectEntity.map(entity -> entity.getTasks()
                        .stream()
                        .map(taskMapper::toDTO)
                        .toList())
                .orElseGet(List::of);
    }
}
