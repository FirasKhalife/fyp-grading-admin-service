package com.fypgrading.adminservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TeamAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(nullable = false)
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
        return this.team.equals(teamAssessment.getTeam())
                && this.assessment.equals(teamAssessment.getAssessment());
    }

    @Override
    public int hashCode() {
        return team.hashCode() + assessment.hashCode();
    }
}
