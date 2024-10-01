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
public class ReviewerRolesDTO {

    private UUID reviewerId;

    private List<ReviewerRoleEnum> roles;
}
