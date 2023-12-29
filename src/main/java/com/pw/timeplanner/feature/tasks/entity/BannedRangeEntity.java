package com.pw.timeplanner.feature.tasks.entity;

import com.pw.timeplanner.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "banned_range")
public class BannedRangeEntity extends BaseEntity {

    private LocalTime startTime;

    private LocalTime endTime;

}
