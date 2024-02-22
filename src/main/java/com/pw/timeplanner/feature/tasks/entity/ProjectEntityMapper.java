package com.pw.timeplanner.feature.tasks.entity;

import com.pw.timeplanner.config.MapStructConfig;
import com.pw.timeplanner.core.entity.JsonNullableMapper;
import com.pw.timeplanner.feature.tasks.api.projectDto.CreateProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.ProjectDTO;
import com.pw.timeplanner.feature.tasks.api.projectDto.UpdateProjectDTO;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = JsonNullableMapper.class)
public interface ProjectEntityMapper {

    ProjectDTO toDTO(ProjectEntity entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    ProjectEntity toEntity(ProjectDTO dto);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "id", ignore = true)
    ProjectEntity createEntity(CreateProjectDTO dto);


    @InheritConfiguration
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    void update(UpdateProjectDTO update, @MappingTarget ProjectEntity entity);

}
