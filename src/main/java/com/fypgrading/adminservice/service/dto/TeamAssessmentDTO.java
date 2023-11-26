package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.entity.TeamAssessment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamAssessmentDTO {

    private TeamDTO team;

    private AssessmentDTO assessment;

    private Float grade;

    public TeamAssessmentDTO(TeamAssessment teamGrade) {
        this.team = new TeamDTO(teamGrade.getTeam());
        this.assessment = new AssessmentDTO(teamGrade.getAssessment());
        this.grade = teamGrade.getGrade();
    }
}
