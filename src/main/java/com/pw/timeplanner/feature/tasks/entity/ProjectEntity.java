package com.pw.timeplanner.feature.tasks.entity;

import com.pw.timeplanner.core.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
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
@Table(name = "project", uniqueConstraints = {@UniqueConstraint(name = "UniqueNameAndUserId",columnNames = { "name", "user_id" })})
public class ProjectEntity extends BaseEntity {
    public ProjectEntity(ProjectEntity entity) {
        this.name = entity.name;
        this.userId = entity.userId;
        this.color = entity.color;
        this.scheduleStartTime = entity.scheduleStartTime;
        this.scheduleEndTime = entity.scheduleEndTime;
        this.tasks = Set.of();
    }

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String userId;

    private String color;

    private LocalTime scheduleStartTime;
    private LocalTime scheduleEndTime;

    @OneToMany(mappedBy="project", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OrderBy("isImportant DESC, isUrgent DESC, name ASC")
    private Set<TaskEntity> tasks;
}
