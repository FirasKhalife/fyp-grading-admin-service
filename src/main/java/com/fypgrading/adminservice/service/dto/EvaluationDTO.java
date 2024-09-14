package com.fypgrading.adminservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDTO {

    private Long reviewerId;

    private Long teamId;

    private List<GradedRubricDTO> gradedRubrics;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EvaluationDTO evaluation)) return false;
        return evaluation.getReviewerId().equals(this.getReviewerId())
                && evaluation.getTeamId().equals(this.getTeamId());
    }

    @Override
    public int hashCode() {
        return this.getReviewerId().hashCode() + this.getTeamId().hashCode();
    }
}
