package com.pw.timeplanner.client.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ScheduleTasksRequest {
    List<Task> tasks;
    List<Project> projects;
    List<BannedRange> bannedRanges;
}
