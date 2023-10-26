package com.pw.timeplanner.feature.tasks.entity;

import com.pw.timeplanner.core.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "task")
public class TaskEntity extends BaseEntity {

    @Column(nullable = false)
    @Setter
    private String name;

    @Setter
    private LocalDate startDate;

    @Setter
    private LocalTime startTime;

    @Setter
    private Integer durationMin;

    @Column(nullable = false)
    private String userId;
}
