package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.service.enums.RoleEnum;
import lombok.Data;

@Data
public class JwtResponse {

    private Integer id;

    private String email;

    private String firstName;

    private String lastName;

    private String accessToken;

    private String tokenType = "Bearer";

    private RoleEnum role;

    public JwtResponse(Integer id, String email, String firstName, String lastName, String accessToken, RoleEnum role) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accessToken = accessToken;
        this.role = role;
    }
}