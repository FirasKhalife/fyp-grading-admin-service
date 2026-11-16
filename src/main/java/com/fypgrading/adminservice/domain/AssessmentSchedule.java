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

/** The due date of one assessment, the same for every team. Late submissions are allowed and marked overdue. */
@Entity
@Table(name = "assessment_schedule")
public class AssessmentSchedule extends AssignedIdEntity {

    @TenantId
    @Column(name = "demo_session", nullable = false, updatable = false)
    private String demoSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Assessment assessment;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    protected AssessmentSchedule() {}

    public AssessmentSchedule(UUID id, Assessment assessment, Instant dueAt) {
        super(id);
        this.assessment = assessment;
        this.dueAt = dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public Instant getDueAt() {
        return dueAt;
    }
}
