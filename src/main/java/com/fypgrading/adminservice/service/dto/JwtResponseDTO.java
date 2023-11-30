package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.entity.Reviewer;
import com.fypgrading.adminservice.service.enums.RoleEnum;
import lombok.Data;

import java.util.List;

@Data
public class JwtResponseDTO {

    private Integer id;

    private String email;

    private String firstName;

    private String lastName;

    private String accessToken;

    private String tokenType = "Bearer";

    private Boolean isAdmin = false;

    private List<RoleEnum> roles;

    public JwtResponseDTO(Reviewer reviewer) {
        this.id = reviewer.getId();
        this.email = reviewer.getEmail();
        this.firstName = reviewer.getFirstName();
        this.lastName = reviewer.getLastName();
        this.accessToken = "accessToken";
        this.isAdmin = reviewer.getIsAdmin() != null ? reviewer.getIsAdmin() : false;
    }

}