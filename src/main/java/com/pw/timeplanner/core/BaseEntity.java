package com.pw.timeplanner.core;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable = false, updatable = false)
    private UUID id;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Column(name = "modified_date", nullable = false)
    private Instant lastModifiedDate;

    @PrePersist
    public void createDates(){
        this.creationDate = Instant.now();
        this.lastModifiedDate = this.creationDate;
    }

    @PreUpdate
    public void updateDate(){
        this.lastModifiedDate = Instant.now();
    }
}
