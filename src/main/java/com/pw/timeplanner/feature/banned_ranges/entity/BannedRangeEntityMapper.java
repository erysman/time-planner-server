package com.pw.timeplanner.feature.banned_ranges.entity;

import com.pw.timeplanner.config.MapStructConfig;
import com.pw.timeplanner.feature.banned_ranges.api.dto.BannedRangeDTO;
import com.pw.timeplanner.feature.banned_ranges.api.dto.CreateBannedRangeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface BannedRangeEntityMapper {

    BannedRangeDTO toDTO(BannedRangeEntity entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    BannedRangeEntity toEntity(BannedRangeDTO dto);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "id", ignore = true)
    BannedRangeEntity createEntity(CreateBannedRangeDTO dto);

}
