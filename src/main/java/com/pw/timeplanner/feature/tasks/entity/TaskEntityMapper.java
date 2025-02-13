package com.pw.timeplanner.feature.tasks.entity;

import com.pw.timeplanner.config.MapStructConfig;
import com.pw.timeplanner.core.entity.JsonNullableMapper;
import com.pw.timeplanner.feature.tasks.api.dto.CreateTaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.UpdateTaskDTO;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(config = MapStructConfig.class, uses = JsonNullableMapper.class)
public interface TaskEntityMapper {

    @Mapping(source = "project", target = "projectId")
    TaskDTO toDTO(TaskEntity entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "dayOrder", ignore = true)
    @Mapping(target = "projectOrder", ignore = true)
    @Mapping(target = "autoScheduled", ignore = true)
    @Mapping(target = "scheduleRunId", ignore = true)
    @Mapping(target = "project", ignore = true)
    TaskEntity toEntity(TaskDTO dto);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dayOrder", ignore = true)
    @Mapping(target = "projectOrder", ignore = true)
    @Mapping(target = "autoScheduled", ignore = true)
    @Mapping(target = "scheduleRunId", ignore = true)
    @Mapping(target = "project", ignore = true)
    TaskEntity createEntity(CreateTaskDTO dto);

    @InheritConfiguration
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dayOrder", ignore = true)
    @Mapping(target = "projectOrder", ignore = true)
    @Mapping(target = "autoScheduled", ignore = true)
    @Mapping(target = "scheduleRunId", ignore = true)
    @Mapping(target = "project", ignore = true)
    void update(UpdateTaskDTO update, @MappingTarget TaskEntity entity);


    default UUID map(ProjectEntity project) {
        return project.getId();
    }
}
