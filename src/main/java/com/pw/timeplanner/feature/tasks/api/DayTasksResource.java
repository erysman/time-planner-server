package com.pw.timeplanner.feature.tasks.api;

import com.pw.timeplanner.feature.tasks.api.dto.ScheduleInfoDTO;
import com.pw.timeplanner.feature.tasks.api.dto.TaskDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

//@Validated
@RequestMapping(DayTasksResource.RESOURCE_PATH)
public interface DayTasksResource {

    String RESOURCE_PATH = "/day/{day}/tasks";
    String ORDER_PATH = "/order";
    String SCHEDULE_PATH = "/schedule";

    @GetMapping
    @Operation(summary = "Get day's tasks", responses = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Unauthorized")})
    List<TaskDTO> getDayTasks(JwtAuthenticationToken authentication, @PathVariable("day") LocalDate day);

    @GetMapping(ORDER_PATH)
    @Operation(summary = "Get order of tasks on day's list", responses = {@ApiResponse(responseCode = "200",
            description = "OK"), @ApiResponse(responseCode = "403", description = "Unauthorized")})
    List<UUID> getTasksDayOrder(JwtAuthenticationToken authentication, @PathVariable("day") LocalDate day);

    @PutMapping(ORDER_PATH)
    @Operation(summary = "Update tasks order on day's list", responses = {@ApiResponse(responseCode = "200",
            description = "OK"), @ApiResponse(responseCode = "403", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Bad request")})
    List<UUID> updateTasksDayOrder(JwtAuthenticationToken authentication, @PathVariable("day") LocalDate day,
                                   @RequestBody List<UUID> positions);

    @GetMapping(SCHEDULE_PATH)
    @Operation(summary = "Get day's auto schedule info", responses = {@ApiResponse(responseCode = "200", description
            = "OK"), @ApiResponse(responseCode = "403", description = "Unauthorized")})
    ScheduleInfoDTO getAutoScheduleInfo(JwtAuthenticationToken authentication, @PathVariable("day") LocalDate day);

    @PostMapping(SCHEDULE_PATH)
    @Operation(summary = "Automatically assign startTime to all tasks for selected day", responses =
            {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "403", description =
                    "Unauthorized")})
    void schedule(JwtAuthenticationToken authentication, @PathVariable("day") LocalDate day);

    @DeleteMapping(SCHEDULE_PATH)
    @Operation(summary = "Remove startTimes for tasks set in last automatic schedule", responses =
            {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "403", description =
                    "Unauthorized")})
    void revokeSchedule(JwtAuthenticationToken authentication, @PathVariable("day") LocalDate day);


}
