package com.pw.timeplanner.feature.projects;

import com.pw.timeplanner.config.MapStructConfig;
import com.pw.timeplanner.core.entity.JsonNullableMapper;
import com.pw.timeplanner.feature.projects.dto.CreateProjectDTO;
import com.pw.timeplanner.feature.projects.dto.ProjectDTO;
import com.pw.timeplanner.feature.projects.dto.UpdateProjectDTO;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = JsonNullableMapper.class)
interface ProjectEntityMapper {

    ProjectDTO toDTO(Project entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    Project toEntity(ProjectDTO dto);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "id", ignore = true)
    Project createEntity(CreateProjectDTO dto);


    @InheritConfiguration
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "id", ignore = true)
    void update(UpdateProjectDTO update, @MappingTarget Project entity);

}
