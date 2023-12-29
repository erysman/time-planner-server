package com.pw.timeplanner.client.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class ScheduleTasksResponse {
    List<ScheduledTask> scheduledTasks;
    UUID runId;
}
