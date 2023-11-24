package com.fypgrading.adminservice.entity;

import com.fypgrading.adminservice.entity.idClass.TeamAssessmentGradeId;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(TeamAssessmentGradeId.class)
public class TeamAssessmentGrade {

    @Id
    @ManyToOne
    private Team team;

    @Id
    @Enumerated(EnumType.STRING)
    private AssessmentEnum assessment;

    private Float grade;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TeamAssessmentGrade teamAssessmentGrade)) return false;
        return this.getTeam().equals(teamAssessmentGrade.getTeam())
                && this.getAssessment().equals(teamAssessmentGrade.getAssessment());
    }

    @Override
    public int hashCode() {
        return getTeam().hashCode() + getAssessment().hashCode();
    }
}
