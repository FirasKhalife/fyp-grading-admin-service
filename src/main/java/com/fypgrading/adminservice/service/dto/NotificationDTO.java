package com.fypgrading.adminservice.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;

    private Long teamId;

    private AssessmentEnum assessment;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private LocalDateTime gradeFinalizedAt;

    @JsonProperty("isRead")
    private boolean isRead = false;

}
