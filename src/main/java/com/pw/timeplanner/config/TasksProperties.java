package com.pw.timeplanner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "features.tasks")
@Data
public class TasksProperties {
    Integer timeGranularityMinutes;
    Integer minDurationMinutes;
    Integer defaultDurationMinutes;
}
