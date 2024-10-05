package com.fypgrading.adminservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamGradedAssessmentsDTO {

    private TeamDTO team;

    private List<GradedAssessmentDTO> gradedAssessments;

}
