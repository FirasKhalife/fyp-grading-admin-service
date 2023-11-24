package com.fypgrading.adminservice.entity.idClass;

import com.fypgrading.adminservice.entity.Team;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamAssessmentGradeId implements Serializable {

    private Team team;

    private AssessmentEnum assessment;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TeamAssessmentGradeId teamAssessmentGradeId)) return false;
        return this.getTeam().equals(teamAssessmentGradeId.getTeam())
                && getAssessment().equals(teamAssessmentGradeId.getAssessment());
    }

    @Override
    public int hashCode() {
        return getTeam().hashCode() + getAssessment().hashCode();
    }
}
