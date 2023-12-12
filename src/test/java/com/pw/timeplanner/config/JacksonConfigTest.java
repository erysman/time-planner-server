package com.pw.timeplanner.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.timeplanner.feature.tasks.api.dto.TaskUpdateDTO;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Testcontainers
class JacksonConfigTest {

    @Autowired
    private ObjectMapper mapper;

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0-debian"));

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.driverClassName", () -> mySQLContainer.getDriverClassName());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
    }

    @Test
    void should_use_json_nullable_module() throws JsonProcessingException {
        assertEquals(JsonNullable.of(LocalDate.of(2023, 11, 30)), mapper.readValue("{\"startDate\":\"2023-11-30\"}", TaskUpdateDTO.class).getStartDay());
        assertEquals(JsonNullable.of(null), mapper.readValue("{\"startDate\":null}", TaskUpdateDTO.class).getStartDay());
        assertEquals(JsonNullable.of(LocalTime.of(11, 11, 30)), mapper.readValue("{\"startTime\":\"11:11:30\"}", TaskUpdateDTO.class).getStartTime());
        assertNull(mapper.readValue("{}", TaskUpdateDTO.class).getStartDay());
    }
}