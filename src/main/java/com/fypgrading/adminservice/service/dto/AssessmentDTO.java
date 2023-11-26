package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.entity.Assessment;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import com.fypgrading.adminservice.service.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDTO {

    private Integer id;

    private AssessmentEnum name;

    private Integer weight;

    private RoleEnum role;

    public AssessmentDTO(AssessmentEnum assessmentEnum) {
        this.id = assessmentEnum.getEnumId();
        this.name = assessmentEnum;
    }

    public AssessmentDTO(Assessment assessment) {
        this.id = assessment.getId();
        this.name = assessment.getName();
        this.weight = assessment.getWeight();
        this.role = assessment.getRole() != null ? assessment.getRole().getName() : null;
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
