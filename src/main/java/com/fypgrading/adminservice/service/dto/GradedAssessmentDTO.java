package com.fypgrading.adminservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradedAssessmentDTO extends AssessmentDTO {

    private Float grade;

    public GradedAssessmentDTO(AssessmentDTO assessment, Float grade) {
        super(assessment.getId(), assessment.getName(), assessment.getWeight(), assessment.getRole());
        this.grade = grade;
    }

}
