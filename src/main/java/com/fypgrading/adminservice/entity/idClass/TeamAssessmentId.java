package com.fypgrading.adminservice.entity.idClass;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamAssessmentId implements Serializable {

    private Team team;

    private Assessment assessment;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TeamAssessmentId teamAssessmentId)) return false;
        return this.getTeam().equals(teamAssessmentId.getTeam())
                && getAssessment().equals(teamAssessmentId.getAssessment());
    }

    @Override
    public int hashCode() {
        return getTeam().hashCode() + getAssessment().hashCode();
    }
}
