package com.pw.timeplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;

import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.core.Conditions.requestTo;

@Configuration
class RequestLogConfig {

    @Bean
    Logbook logbook() {
        return Logbook.builder()
                .condition(exclude(
                        requestTo("/actuator/health"),
                        requestTo("/api-docs/**"),
                        requestTo("/swagger-ui/**")
                ))
                .build();
    }

}

