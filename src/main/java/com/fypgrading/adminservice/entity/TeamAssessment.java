package com.fypgrading.adminservice.entity;

import com.fypgrading.adminservice.entity.idClass.TeamAssessmentId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(TeamAssessmentId.class)
public class TeamAssessment {

    @Id
    @ManyToOne(cascade = CascadeType.MERGE)
    private Team team;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private Assessment assessment;

    private Float grade;

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
