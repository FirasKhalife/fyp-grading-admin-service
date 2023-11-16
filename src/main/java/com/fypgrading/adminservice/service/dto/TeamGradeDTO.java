package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamGradeDTO {

    private AssessmentEnum assessment;

    private TeamDTO team;

    private List<ReviewerTeamGradeDTO> teamReviewersGrades;

    public void addReviewerGrade(ReviewerTeamGradeDTO teamReviewersGrade) {
        this.teamReviewersGrades.add(teamReviewersGrade);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TeamGradeDTO teamGrade)) return false;
        return team.getId().equals(teamGrade.team.getId());
    }

    @Override
    public int hashCode() {
        return team.getId();
    }

}
