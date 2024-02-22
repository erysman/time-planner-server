package com.pw.timeplanner.scheduling_client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor
public class ScheduleTasksResponse {
    List<ScheduledTask> scheduledTasks;
    UUID runId;
}
