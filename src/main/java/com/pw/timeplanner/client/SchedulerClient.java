package com.pw.timeplanner.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.timeplanner.client.model.BannedRange;
import com.pw.timeplanner.client.model.Project;
import com.pw.timeplanner.client.model.ScheduleTasksRequest;
import com.pw.timeplanner.client.model.ScheduleTasksResponse;
import com.pw.timeplanner.client.model.Task;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class SchedulerClient {

    private RestClient client;
    private ObjectMapper mapper;

    public ScheduleTasksResponse scheduleTasks(List<Task> tasks, List<Project> projects, List<BannedRange> bannedRanges) {
        log.info("Triggering autoschedule service with tasks {}, projects {} and bannedRanges {}", tasks, projects, bannedRanges);
        ScheduleTasksRequest scheduleTasksRequest = ScheduleTasksRequest.builder()
                .tasks(tasks)
                .projects(projects)
                .bannedRanges(bannedRanges)
                .build();
        try {
            String jsonRequest = mapper.writeValueAsString(scheduleTasksRequest);
            ScheduleTasksResponse scheduleTasksResponse = client.post()
                    .uri("/scheduleTasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonRequest)
                    .retrieve()
                    .onStatus(org.springframework.http.HttpStatusCode::isError, (request, response) -> {
                        throw new ScheduleServiceException(response.getStatusCode().toString(), response.getHeaders().toString());
                    })
                    .body(ScheduleTasksResponse.class);
            return scheduleTasksResponse;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
