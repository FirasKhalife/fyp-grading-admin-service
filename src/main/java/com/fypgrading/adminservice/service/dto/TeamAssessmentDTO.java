package com.fypgrading.adminservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamAssessmentDTO {

    private TeamDTO team;

    private AssessmentDTO assessment;

    private Float grade;

}
