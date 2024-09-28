package com.fypgrading.adminservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentGradeDTO {

    private Long id;

    private AssessmentDTO assessment;

    private Float grade;

    private UUID reviewerId;

    private Long teamId;
}

