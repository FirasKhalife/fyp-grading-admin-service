package com.fypgrading.adminservice.service.event;

import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllReviewersSubmittedEvent {

    private Integer teamId;

    private AssessmentEnum assessment;
}
