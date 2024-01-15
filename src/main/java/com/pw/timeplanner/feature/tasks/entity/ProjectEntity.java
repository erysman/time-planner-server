package com.pw.timeplanner.feature.tasks.entity;

import com.pw.timeplanner.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "project")
public class ProjectEntity extends BaseEntity {

    @Column(nullable = false, unique=true)
    private String name;

    @Column(nullable = false)
    private String userId;

    private String color;

    @Builder.Default
    private LocalTime scheduleStartTime = LocalTime.MIN;

    @Builder.Default
    private LocalTime scheduleEndTime= LocalTime.MAX;

    @OneToMany(mappedBy="project", fetch = FetchType.LAZY)
    @OrderBy("isImportant DESC, isUrgent DESC, name ASC")
    private Set<TaskEntity> tasks;
}
