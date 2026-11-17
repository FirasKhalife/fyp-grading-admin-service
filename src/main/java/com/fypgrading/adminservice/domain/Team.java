package com.fypgrading.adminservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

/** A group of students doing one project, with the final grade once all five assessments are final. */
@Entity
@Table(name = "team")
public class Team extends Audited {

    @TenantId
    @Column(name = "demo_session", nullable = false, updatable = false)
    private String demoSession;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(name = "project_title", nullable = false, length = 200)
    private String projectTitle;

    @Column(length = 2000)
    private String description;

    @Column(name = "final_grade")
    private Double finalGrade;

    @Column(name = "final_computed_at")
    private Instant finalComputedAt;

    protected Team() {}

    public Team(UUID id, String name, String projectTitle, String description) {
        super(id);
        this.name = name;
        this.projectTitle = projectTitle;
        this.description = description;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFinalGrade(Double finalGrade, Instant at) {
        this.finalGrade = finalGrade;
        this.finalComputedAt = finalGrade == null ? null : at;
    }

    public String getName() {
        return name;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public String getDescription() {
        return description;
    }

    public Double getFinalGrade() {
        return finalGrade;
    }

    public Instant getFinalComputedAt() {
        return finalComputedAt;
    }
}
