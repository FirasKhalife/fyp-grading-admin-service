package com.fypgrading.adminservice.service.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AssessmentEnum {

    @JsonProperty("ORAL_PROPOSAL")
    ORAL_PROPOSAL(RoleEnum.JURY_MEMBER),

    @JsonProperty("PROGRESS")
    PROGRESS(RoleEnum.JURY_MEMBER),

    @JsonProperty("PROGRESS_REPORT")
    ADVISOR(RoleEnum.ADVISOR),

    @JsonProperty("FINAL_REPORT")
    FINAL_REPORT(RoleEnum.JURY_MEMBER),

    @JsonProperty("FINAL_PRESENTATION")
    FINAL_PRESENTATION(RoleEnum.JURY_MEMBER);

    private final RoleEnum responsibleRole;
}
