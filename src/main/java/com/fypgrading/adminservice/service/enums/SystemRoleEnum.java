package com.fypgrading.adminservice.service.enums;

import lombok.Getter;

@Getter
public enum SystemRoleEnum {
    ROLE_ADMIN,
    ROLE_DEV;

    public static final class Names {
        public static final String ROLE_ADMIN = "ADMIN";
        public static final String ROLE_DEV = "DEV";
    }

}
