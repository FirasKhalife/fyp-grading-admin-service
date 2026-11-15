package com.fypgrading.adminservice.domain;

import com.fypgrading.adminservice.common.jpa.AssignedIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

/** When and where a team presents its final defense. The jury is the team's jury members. */
@Entity
@Table(name = "defense_slot")
public class DefenseSlot extends AssignedIdEntity {

    @TenantId
    @Column(name = "demo_session", nullable = false, updatable = false)
    private String demoSession;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(nullable = false, length = 60)
    private String room;

    protected DefenseSlot() {
    }

    public DefenseSlot(UUID id, UUID teamId, Instant startsAt, Instant endsAt, String room) {
        super(id);
        this.teamId = teamId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.room = room;
    }

    public void move(Instant startsAt, Instant endsAt, String room) {
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.room = room;
    }

    public boolean overlaps(DefenseSlot other) {
        return startsAt.isBefore(other.endsAt) && other.startsAt.isBefore(endsAt);
    }

    public UUID getTeamId() {
        return teamId;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public String getRoom() {
        return room;
    }
}
