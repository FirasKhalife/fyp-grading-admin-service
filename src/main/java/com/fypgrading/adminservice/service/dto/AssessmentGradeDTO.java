package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentGradeDTO {

    private Long id;

    private AssessmentDTO assessment;

    private Float grade;

    private Long reviewerId;

    private Long teamId;
}

