package com.fypgrading.adminservice.service.event;

import com.fypgrading.adminservice.service.dto.TeamDTO;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeFinalizedEvent {

    private TeamDTO team;

    private AssessmentEnum assessment;

    private LocalDateTime gradeFinalizedAt;
}
