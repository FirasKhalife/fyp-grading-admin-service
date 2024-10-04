package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.constants.Constants;
import com.fypgrading.adminservice.service.event.EvaluationSubmittedEvent;
import com.fypgrading.adminservice.service.event.GradeFinalizedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class EventDispatcher {

    private final StreamBridge streamBridge;

    public void sendAdminNotification(GradeFinalizedEvent event) {
        log.debug("Sending GradeFinalizedEvent to {}: {}", Constants.SEND_NOTIFICATION_OUT, event);
        streamBridge.send(Constants.SEND_NOTIFICATION_OUT, event);
    }

    public void aggregateGrades(EvaluationSubmittedEvent event) {
        log.debug("Sending EvaluationSubmittedEvent to {}: {}", Constants.AGGREGATE_GRADES_OUT, event);
        streamBridge.send(Constants.AGGREGATE_GRADES_OUT, event);
    }
}
