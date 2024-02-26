package com.pw.timeplanner.feature.tasks;

import com.pw.timeplanner.core.entity.BaseEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "task", indexes = {
        @Index(name = "idxTaskUserIdStartDay", columnList = "userId, startDay"),
        @Index(name = "idxTaskProjectId", columnList = "project_id")
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@ToString(callSuper = true, exclude = {"project"})
class Task extends BaseEntity {

    @Column(nullable = false)
    @Setter
    private String name;

    @Setter
    private LocalDate startDay;

    @Setter
    private LocalTime startTime;

    @Setter
    private Integer durationMin;

    @Setter
    private Integer dayOrder;

    @Setter
    private Integer projectOrder;

    @Column(nullable = false)
    private String userId;

    @Builder.Default
    @Column(columnDefinition = "boolean default false")
    @Setter
    private Boolean autoScheduled = false;

    @Builder.Default
    @Setter
    private Boolean isImportant = false;

    @Builder.Default
    @Setter
    private Boolean isUrgent = false;
    private UUID scheduleRunId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumns(value = {@JoinColumn(name = "project_id", nullable = false)}, foreignKey = @ForeignKey(value =
//            ConstraintMode.NO_CONSTRAINT))
    @Setter
//    @NotFound(action = NotFoundAction.IGNORE)
    private UUID projectId;

    void schedule(UUID runId, LocalTime newStartTime) {
        autoScheduled = true;
        scheduleRunId = runId;
        dayOrder = null; //TODO: this should not be here
        startTime = newStartTime;
    }

    void unschedule() {
        autoScheduled = false;
        scheduleRunId = null;
        startTime = null;
    }
}

