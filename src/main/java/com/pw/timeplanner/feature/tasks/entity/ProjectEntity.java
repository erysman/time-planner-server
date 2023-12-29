package com.pw.timeplanner.feature.tasks.entity;

import com.pw.timeplanner.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "project")
public class ProjectEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private LocalTime scheduleStartTime;

    private LocalTime scheduleEndTime;

    @OneToMany(mappedBy="project", fetch = FetchType.LAZY)
//    @JoinColumns(value = {}, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private Set<TaskEntity> tasks;
}
