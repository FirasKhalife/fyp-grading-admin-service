package com.fypgrading.adminservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A visitor's private copy of the demo data. "Today" in the copy (the anchor) is the middle defense day. */
@Entity
@Table(name = "demo_copy")
public class DemoCopy {

    @Id
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "anchor_date", nullable = false)
    private LocalDate anchorDate;

    protected DemoCopy() {}

    public DemoCopy(UUID id, Instant createdAt, LocalDate anchorDate) {
        this.id = id;
        this.createdAt = createdAt;
        this.anchorDate = anchorDate;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public LocalDate getAnchorDate() {
        return anchorDate;
    }
}
