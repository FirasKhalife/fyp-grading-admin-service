package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradedEvaluationDTO {

    private ReviewerDTO reviewer;

    private TeamDTO team;

    private AssessmentDTO assessment;

    private List<GradedRubricDTO> gradedRubrics;

    private Float grade;

}
