package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerLoginDTO {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private List<RoleEnum> roles;

}
