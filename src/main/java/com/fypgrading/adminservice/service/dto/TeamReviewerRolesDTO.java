package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.ReviewerRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamReviewerRolesDTO {

    private UUID reviewerId;

    private Long teamId;

    private List<ReviewerRoleEnum> roles;
}
