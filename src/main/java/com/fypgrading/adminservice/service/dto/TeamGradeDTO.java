package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamGradeDTO {

    private Integer teamId;

    private Integer reviewerId;

    private AssessmentEnum assessment;

    private Float grade;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TeamGradeDTO teamGrade)) return false;
        return teamGrade.getTeamId().equals(this.getTeamId())
                && teamGrade.getReviewerId().equals(this.getReviewerId());
    }

    @Override
    public int hashCode() {
        return this.getTeamId() + this.getReviewerId();
    }
}