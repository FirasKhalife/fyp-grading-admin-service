package com.fypgrading.adminservice.entity;

import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationSubmissionDTO {

    private Integer teamId;

    private Integer reviewerId;

    private AssessmentEnum assessment;

    private Float grade;
}