package com.pw.timeplanner.client.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@AllArgsConstructor
public class SchedulerClientConfig {

    private SchedulerClientProperties properties;

    @Bean
    RestClient client() {
        return RestClient.builder().baseUrl(properties.getBaseUrl()).build();
    }
}
