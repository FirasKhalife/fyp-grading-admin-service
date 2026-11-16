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

/**
 * One reviewer's submitted score for a team and an assessment (0-100, unrounded). Unique per evaluation, so a
 * retried hand-off from evaluation-service is recognised and never counted twice.
 */
@Entity
@Table(name = "grade")
public class Grade extends AssignedIdEntity {

    @TenantId
    @Column(name = "demo_session", nullable = false, updatable = false)
    private String demoSession;

    @Column(name = "evaluation_id", nullable = false)
    private UUID evaluationId;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Assessment assessment;

    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;

    @Column(nullable = false)
    private double score;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    protected Grade() {}

    public Grade(
            UUID id,
            UUID evaluationId,
            UUID teamId,
            Assessment assessment,
            UUID reviewerId,
            double score,
            Instant submittedAt) {
        super(id);
        this.evaluationId = evaluationId;
        this.teamId = teamId;
        this.assessment = assessment;
        this.reviewerId = reviewerId;
        this.score = score;
        this.submittedAt = submittedAt;
    }

    public UUID getEvaluationId() {
        return evaluationId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public double getScore() {
        return score;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }
}
