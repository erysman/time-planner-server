package com.pw.timeplanner.feature.tasks.entity;

import com.pw.timeplanner.core.entity.BaseEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "project", uniqueConstraints = {@UniqueConstraint(name = "UniqueUserIdAndName",columnNames = { "userId", "name"})})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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

    @Builder.Default
    private LocalTime scheduleStartTime = LocalTime.of(0, 0, 0);
    @Builder.Default
    private LocalTime scheduleEndTime = LocalTime.of(23, 59, 59);

    @OneToMany(mappedBy="project", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OrderBy("isImportant DESC, isUrgent DESC, name ASC" )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<TaskEntity> tasks;
}
