package com.pw.timeplanner.feature.banned_ranges.api;

import com.pw.timeplanner.feature.banned_ranges.api.dto.BannedRangeDTO;
import com.pw.timeplanner.feature.banned_ranges.api.dto.CreateBannedRangeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//@Validated
@RequestMapping(BannedRangesResource.RESOURCE_PATH)
public interface BannedRangesResource {

    String RESOURCE_PATH = "/bannedRanges";

    @GetMapping
    @Operation(summary = "Get banned ranges", responses = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Unauthorized")})
    List<BannedRangeDTO> getBannedRanges(JwtAuthenticationToken authentication);

    @GetMapping("/{id}")
    Optional<BannedRangeDTO> getBannedRange(JwtAuthenticationToken authentication, @PathVariable("id") UUID id);

    @DeleteMapping("/{id}")
    void deleteBannedRange(JwtAuthenticationToken authentication, @PathVariable("id") UUID id);

    @PostMapping
    BannedRangeDTO createBannedRange(JwtAuthenticationToken authentication,
                             @RequestBody @Validated CreateBannedRangeDTO createBannedRangeDTO);

}
