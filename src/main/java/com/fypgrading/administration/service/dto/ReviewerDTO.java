package com.fypgrading.administration.service.dto;

import com.fypgrading.administration.service.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerDTO {

    private String firstName;

    private String lastName;

    private String email;

    private Role role;
}
