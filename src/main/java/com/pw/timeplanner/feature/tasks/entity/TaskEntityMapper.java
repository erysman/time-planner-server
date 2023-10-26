package com.pw.timeplanner.feature.tasks.entity;

import com.pw.timeplanner.api.dto.TaskDTO;
import com.pw.timeplanner.api.dto.TaskUpdateDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TaskEntityMapper {

    TaskDTO toDTO(TaskEntity entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    TaskEntity toEntity(TaskDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    TaskEntity updateEntityFromDTO(TaskUpdateDTO dto, @MappingTarget TaskEntity entity);
}
