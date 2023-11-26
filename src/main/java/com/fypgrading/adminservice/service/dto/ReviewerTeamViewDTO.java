package com.fypgrading.adminservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerTeamViewDTO {

    private TeamDTO team;

    private List<TeamReviewerAssessmentDTO> teamAssessments;

}
