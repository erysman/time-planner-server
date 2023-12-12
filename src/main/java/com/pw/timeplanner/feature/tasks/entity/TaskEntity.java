package com.pw.timeplanner.feature.tasks.entity;

import com.pw.timeplanner.core.entity.BaseEntity;
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
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "task")
public class TaskEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private LocalDate startDay;

    private LocalTime startTime;

    private Integer durationMin;

    private Integer dayOrder;

    @Column(nullable = false)
    private String userId;
}
