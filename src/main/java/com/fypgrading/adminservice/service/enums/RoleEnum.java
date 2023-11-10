package com.fypgrading.adminservice.service.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum RoleEnum {
    @JsonProperty("admin")
    ADMIN,
    @JsonProperty("jury_member")
    JURY_MEMBER,
    @JsonProperty("advisor")
    ADVISOR;
}
