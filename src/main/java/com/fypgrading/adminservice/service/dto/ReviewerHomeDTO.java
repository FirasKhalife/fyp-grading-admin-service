package com.fypgrading.adminservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerHomeDTO {

    private ReviewerTeamsAssessmentsDTO reviewerTeamsAssessments;

    private List<NotificationDTO> notifications;

}
