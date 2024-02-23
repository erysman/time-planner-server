package com.pw.timeplanner.feature.banned_ranges.controller;

import com.pw.timeplanner.feature.banned_ranges.api.BannedRangesResource;
import com.pw.timeplanner.feature.banned_ranges.api.dto.BannedRangeDTO;
import com.pw.timeplanner.feature.banned_ranges.api.dto.CreateBannedRangeDTO;
import com.pw.timeplanner.feature.banned_ranges.service.BannedRangesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.pw.timeplanner.core.AuthUtils.getUserIdFromToken;

@Slf4j
@RestController
@RequiredArgsConstructor
class BannedRangesController implements BannedRangesResource {

    private final BannedRangesService service;
    @Override
    public List<BannedRangeDTO> getBannedRanges(JwtAuthenticationToken authentication) {
        String userId = getUserIdFromToken(authentication);
        return service.getBannedRanges(userId);
    }

    @Override
    public Optional<BannedRangeDTO> getBannedRange(JwtAuthenticationToken authentication, UUID id) {
        String userId = getUserIdFromToken(authentication);
        return service.getBannedRange(userId, id);
    }

    @Override
    public void deleteBannedRange(JwtAuthenticationToken authentication, UUID id) {
        String userId = getUserIdFromToken(authentication);
        service.deleteBannedRange(userId, id);
    }

    @Override
    public BannedRangeDTO createBannedRange(JwtAuthenticationToken authentication, CreateBannedRangeDTO createBannedRangeDTO) {
        String userId = getUserIdFromToken(authentication);
        return service.createBannedRange(userId, createBannedRangeDTO);
    }
}
