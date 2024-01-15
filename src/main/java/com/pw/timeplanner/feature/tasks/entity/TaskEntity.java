package com.pw.timeplanner.feature.tasks.entity;

import com.pw.timeplanner.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

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

    @Builder.Default
    @Column(columnDefinition = "boolean default false")
    private Boolean autoScheduled = false;

    @Builder.Default
    private Boolean isImportant = false;
    @Builder.Default
    private Boolean isUrgent = false;

    private UUID scheduleRunId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = {@JoinColumn(name = "project_id", nullable = false)}, foreignKey = @ForeignKey(value =
            ConstraintMode.NO_CONSTRAINT))
    private ProjectEntity project;

    public Priority getPriority() {
        if (isImportant && isUrgent) return Priority.IMPORTANT_URGENT;
        if (isImportant) return Priority.IMPORTANT;
        if (isUrgent) return Priority.URGENT;
        return Priority.NORMAL;
    }
}

