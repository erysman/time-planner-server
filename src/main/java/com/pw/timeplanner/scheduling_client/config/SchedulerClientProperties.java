package com.pw.timeplanner.scheduling_client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "client.scheduler")
@Data
public class SchedulerClientProperties {
    String baseUrl;
    Integer connectionTimeout;

}
