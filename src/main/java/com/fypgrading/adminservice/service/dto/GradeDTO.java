package com.fypgrading.adminservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeDTO {

    private TeamDTO team;

    private ReviewerDTO reviewer;

    private AssessmentDTO assessment;

    private Float grade;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GradeDTO teamGrade)) return false;
        return teamGrade.getTeam().equals(this.getTeam())
                && teamGrade.getReviewer().equals(this.getReviewer());
    }

    @Override
    public int hashCode() {
        return this.getTeam().hashCode() + this.getReviewer().hashCode();
    }
}