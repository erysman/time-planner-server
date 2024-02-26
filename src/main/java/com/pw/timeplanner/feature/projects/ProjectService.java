package com.pw.timeplanner.feature.projects;

import com.pw.timeplanner.config.TasksProperties;
import com.pw.timeplanner.core.exception.ResourceAlreadyExistsException;
import com.pw.timeplanner.core.exception.ResourceNotFoundException;
import com.pw.timeplanner.feature.projects.dto.CreateProjectDTO;
import com.pw.timeplanner.feature.projects.dto.ProjectDTO;
import com.pw.timeplanner.feature.projects.dto.UpdateProjectDTO;
import com.pw.timeplanner.feature.tasks.TasksProjectOrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    public static final LocalTime SCHEDULE_END_TIME = LocalTime.of(23, 59, 59);

    private final ProjectsRepository projectsRepository;
    private final ProjectEntityMapper mapper;
    private final ProjectsValidator projectsValidator;
    private final TasksProjectOrderService tasksOrderService;
    private final TasksProperties properties;

    public List<ProjectDTO> getProjects(String userId) {
        log.info("Getting projects for user: {}", userId);
        return projectsRepository.findAllByUserId(userId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    public ProjectDTO getProject(String userId, UUID projectId) {
        return mapper.toDTO(getProjectQuery(userId, projectId));
    }

    public ProjectDTO getOrCreateDefaultProject(String userId) {
        return mapper.toDTO(getOrCreateDefaultProjectQuery(userId));
    }

    private Project getProjectQuery(String userId, UUID projectId) {
        log.info("Getting project with id: {} for user: {}", projectId, userId);
        return projectsRepository.findOneByUserIdAndId(userId, projectId)
                .orElseThrow( () -> new ResourceNotFoundException(ProjectsResource.RESOURCE_PATH, projectId));
    }

    private Project getOrCreateDefaultProjectQuery(String userId) {
        return projectsRepository.findOneByUserIdAndName(userId,
                properties.getDefaultProjectName()).or(() -> Optional.of(createDefaultProject(userId))).orElseThrow();
    }

    private Project createDefaultProject(String userId) {
        Project defaultProject = Project.builder()
                .name(properties.getDefaultProjectName())
                .userId(userId)
                .color(properties.getDefaultProjectColor())
                .scheduleStartTime(LocalTime.MIN)
                .scheduleEndTime(SCHEDULE_END_TIME)
                .build();
        log.info("Creating default project named '{}' for user: {}", properties.getDefaultProjectName(), userId);
        return projectsRepository.save(defaultProject);
    }

    @Transactional
    public void deleteProject(String userId, UUID projectId) {
        log.info("Deleting project with id: {} for user: {}", projectId, userId);
        Project project = projectsRepository.findOneByUserIdAndId(userId, projectId)
                .orElseThrow( () -> new ResourceNotFoundException(ProjectsResource.RESOURCE_PATH, projectId));
        boolean existsAndIsDefaultProject = project.getName()
                .equals(properties.getDefaultProjectName());
        if (existsAndIsDefaultProject) {
            Project copy = new Project(project);
            projectsRepository.delete(project);
            projectsRepository.flush();
            projectsRepository.save(copy);
            return;
        }
        projectsRepository.delete(project);
        //TODO: delete all tasks for project
        throw new IllegalStateException("Not implemented");
    }

    @Transactional
    public ProjectDTO updateProject(String userId, UUID projectId, UpdateProjectDTO updateProjectDTO) {
        log.info("Updating project with id: {} for user: {}", projectId, userId);
        Project project = projectsRepository.findOneByUserIdAndId(userId, projectId)
                .orElseThrow( () -> new ResourceNotFoundException(ProjectsResource.RESOURCE_PATH, projectId));
        projectsValidator.validate(updateProjectDTO, project);
        mapper.update(updateProjectDTO, project);
        return mapper.toDTO(project);
    }

    public ProjectDTO createProject(String userId, CreateProjectDTO createProjectDTO) {
        log.info("Creating project for user: {}", userId);
        projectsValidator.validate(createProjectDTO);
        Project entity = mapper.createEntity(createProjectDTO);
        entity.setUserId(userId);
        if(entity.getScheduleStartTime() == null) {
            entity.setScheduleStartTime(LocalTime.MIN);
        }
        if(entity.getScheduleEndTime() == null) {
            entity.setScheduleEndTime(SCHEDULE_END_TIME);
        }
        try {
            return mapper.toDTO(projectsRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            log.info("Project with name: {} already exists for user: {}", createProjectDTO.getName(), userId);
            throw new ResourceAlreadyExistsException(ProjectsResource.RESOURCE_PATH, "name", createProjectDTO.getName());
        }
    }

    public List<UUID> getTasksOrderForProject(String userId, UUID id) {
        Project project = getProjectQuery(userId, id);
        return tasksOrderService.getOrder(userId, project.getId());
    }

    public List<UUID> updateTasksOrderForProject(String userId, UUID id, List<UUID> positions) {
        Project project = getProjectQuery(userId, id);
        return tasksOrderService.reorder(userId, project.getId(), positions);
    }

    public List<ProjectDTO> getProjectsByIds(final String userId, final List<UUID> projectIds) {
        return projectsRepository.findAllByUserIdAndIdIsIn(userId, projectIds)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }
}
