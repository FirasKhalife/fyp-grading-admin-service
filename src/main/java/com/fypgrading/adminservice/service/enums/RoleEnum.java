package com.fypgrading.adminservice.service.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    ADMIN,
    JURY_MEMBER,
    ADVISOR;

    public long getInstanceId() {
        return ordinal() + 1L;
    }

}
