package com.fypgrading.adminservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamReviewerAssessmentDTO extends AssessmentDTO {

    private Float grade;

    public TeamReviewerAssessmentDTO(AssessmentDTO assessment) {
        super(assessment.getId(), assessment.getName(), assessment.getWeight(), assessment.getRole());
    }

}
