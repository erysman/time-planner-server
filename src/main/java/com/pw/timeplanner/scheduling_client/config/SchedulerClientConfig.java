package com.pw.timeplanner.scheduling_client.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@AllArgsConstructor
class SchedulerClientConfig {

    private final SchedulerClientProperties properties;

    @Bean
    RestClient client(RestTemplateBuilder restTemplateBuilder) {
        return RestClient.builder(
                restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(properties.getConnectionTimeout()))
                        .setReadTimeout(Duration.ofSeconds(properties.getConnectionTimeout()))
                        .rootUri(properties.getBaseUrl())
                        .build())
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
