package com.fypgrading.adminservice.domain;

import com.fypgrading.adminservice.common.jpa.AssignedIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

/** A team's grade on one assessment: final once every expected reviewer submitted, then released to students. */
@Entity
@Table(name = "team_assessment")
public class TeamAssessment extends AssignedIdEntity {

    @TenantId
    @Column(name = "demo_session", nullable = false, updatable = false)
    private String demoSession;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Assessment assessment;

    @Column(nullable = false)
    private double grade;

    @Column(name = "finalized_at", nullable = false)
    private Instant finalizedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    protected TeamAssessment() {}

    public TeamAssessment(UUID id, UUID teamId, Assessment assessment, double grade, Instant finalizedAt) {
        super(id);
        this.teamId = teamId;
        this.assessment = assessment;
        this.grade = grade;
        this.finalizedAt = finalizedAt;
    }

    public void regrade(double grade) {
        this.grade = grade;
    }

    public void release(Instant at) {
        this.releasedAt = at;
    }

    public boolean isReleased() {
        return releasedAt != null;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public double getGrade() {
        return grade;
    }

    public Instant getFinalizedAt() {
        return finalizedAt;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }
}
