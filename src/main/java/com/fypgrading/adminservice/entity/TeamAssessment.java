package com.fypgrading.adminservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class TeamAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private Assessment assessment;

    private Float grade;

    public TeamAssessment(Team team, Assessment assessment, Float grade) {
        this.team = team;
        this.assessment = assessment;
        this.grade = grade;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TeamAssessment teamAssessment)) return false;
        return this.getTeam().equals(teamAssessment.getTeam())
                && this.getAssessment().equals(teamAssessment.getAssessment());
    }

    @Override
    public int hashCode() {
        return getTeam().hashCode() + getAssessment().hashCode();
    }
}
