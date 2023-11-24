package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.RoleEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JwtResponseDTO {

    private Integer id;

    private String email;

    private String firstName;

    private String lastName;

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    @Builder.Default
    private Boolean isAdmin = false;

    private List<RoleEnum> roles;

}