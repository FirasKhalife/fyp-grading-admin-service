package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerTeamRoleDTO {

    private Integer teamId;

    private RoleEnum role;

}
