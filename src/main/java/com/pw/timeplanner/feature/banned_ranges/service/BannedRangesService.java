package com.pw.timeplanner.feature.banned_ranges.service;

import com.pw.timeplanner.feature.banned_ranges.api.dto.BannedRangeDTO;
import com.pw.timeplanner.feature.banned_ranges.api.dto.CreateBannedRangeDTO;
import com.pw.timeplanner.feature.banned_ranges.repository.BannedRangeRepository;
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
    public List<BannedRangeDTO> getBannedRanges(String userId) {
//        return repository.findAllByUserId(userId);
        return List.of();
    }

    public Optional<BannedRangeDTO> getBannedRange(String userId, UUID id) {
        return Optional.empty();
    }

    public void deleteBannedRange(String userId, UUID id) {

    }

    public BannedRangeDTO createBannedRange(String userId, CreateBannedRangeDTO createBannedRangeDTO) {
        return null;
    }
}
