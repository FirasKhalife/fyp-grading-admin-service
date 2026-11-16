package com.fypgrading.adminservice.domain;

import com.fypgrading.adminservice.common.jpa.AssignedIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

/** A reviewer on a team, with one role (jury or advisor) on that team. */
@Entity
@Table(name = "team_reviewer")
public class TeamReviewer extends AssignedIdEntity {

    @TenantId
    @Column(name = "demo_session", nullable = false, updatable = false)
    private String demoSession;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReviewerRole role;

    protected TeamReviewer() {}

    public TeamReviewer(UUID id, UUID teamId, UUID reviewerId, ReviewerRole role) {
        super(id);
        this.teamId = teamId;
        this.reviewerId = reviewerId;
        this.role = role;
    }

    public void changeRole(ReviewerRole role) {
        this.role = role;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public ReviewerRole getRole() {
        return role;
    }
}
