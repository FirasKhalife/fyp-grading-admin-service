package com.fypgrading.adminservice.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AssessmentEnum {
    PROPOSAL_PRESENTATION,
    PROGRESS_REPORT,
    ADVISOR_ASSESSMENT,
    FINAL_REPORT,
    FINAL_PRESENTATION;

    public long getInstanceId() {
        return ordinal() + 1L;
    }
}
