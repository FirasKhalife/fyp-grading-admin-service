package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentGradeDTO {

    private Integer id;

    private AssessmentEnum assessment;

    private Float grade;

    private Integer reviewerId;

    private Integer teamId;
}

