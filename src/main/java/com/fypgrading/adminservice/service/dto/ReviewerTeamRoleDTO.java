package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.ReviewerRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerTeamRoleDTO {

    private Long teamId;

    private ReviewerRoleEnum role;

}
