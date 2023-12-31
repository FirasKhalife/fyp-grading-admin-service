package com.fypgrading.adminservice.service.event;

import com.fypgrading.adminservice.service.dto.AssessmentDTO;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationSubmittedEvent {

    private TeamDTO team;

    private AssessmentDTO assessment;

}
