package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import com.fypgrading.adminservice.service.enums.ReviewerRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDTO {

    private Long id;

    private AssessmentEnum name;

    private Integer weight;

    private ReviewerRoleEnum role;

    public AssessmentDTO(AssessmentEnum assessmentEnum) {
        this.id = assessmentEnum.getInstanceId();
        this.name = assessmentEnum;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AssessmentDTO assessment)) return false;
        return this.getId().equals(assessment.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
