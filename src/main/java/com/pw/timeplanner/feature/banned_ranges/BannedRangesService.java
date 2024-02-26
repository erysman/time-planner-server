package com.pw.timeplanner.feature.banned_ranges;

import com.pw.timeplanner.feature.banned_ranges.api.dto.BannedRangeDTO;
import com.pw.timeplanner.feature.banned_ranges.api.dto.CreateBannedRangeDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class BannedRangesService {

    private final BannedRangeRepository repository;
    private final BannedRangeEntityMapper mapper;

    public List<BannedRangeDTO> getBannedRanges(String userId) {
        return repository.findAllByUserId(userId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    public Optional<BannedRangeDTO> getBannedRange(String userId, UUID id) {
        Optional<BannedRangeEntity> bannedRangeEntity = repository.findByUserIdAndId(userId, id);
        return bannedRangeEntity.map(mapper::toDTO);
    }

    public void deleteBannedRange(String userId, UUID id) {
        Optional<BannedRangeEntity> bannedRangeEntity = repository.findByUserIdAndId(userId, id);
        bannedRangeEntity.ifPresent(repository::delete);
    }

    public BannedRangeDTO createBannedRange(String userId, CreateBannedRangeDTO createBannedRangeDTO) {
        BannedRangeEntity bannedRangeEntity = mapper.createEntity(createBannedRangeDTO);
        bannedRangeEntity.setUserId(userId);
        return mapper.toDTO(repository.save(bannedRangeEntity));
    }
}
