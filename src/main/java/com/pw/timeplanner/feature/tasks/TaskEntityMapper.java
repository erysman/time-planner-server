package com.pw.timeplanner.feature.tasks;

import com.pw.timeplanner.config.MapStructConfig;
import com.pw.timeplanner.core.entity.JsonNullableMapper;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import com.pw.timeplanner.feature.tasks.api.dto.UpdateTaskDTO;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class, uses = JsonNullableMapper.class)
interface TaskEntityMapper {
    TaskDTO toDTO(Task entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "dayOrder", ignore = true)
    @Mapping(target = "projectOrder", ignore = true)
    @Mapping(target = "autoScheduled", ignore = true)
    @Mapping(target = "scheduleRunId", ignore = true)
    Task toEntity(TaskDTO dto);

    @InheritConfiguration
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dayOrder", ignore = true)
    @Mapping(target = "projectOrder", ignore = true)
    @Mapping(target = "autoScheduled", ignore = true)
    @Mapping(target = "scheduleRunId", ignore = true)
    void update(UpdateTaskDTO update, @MappingTarget Task entity);

}
