package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.service.event.EvaluationSubmittedEvent;
import com.fypgrading.adminservice.service.event.GradeFinalizedEvent;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class EventDispatcher {

    private final Logger logger = LoggerFactory.getLogger(EventDispatcher.class);
    private final StreamBridge streamBridge;

    public void sendAdminNotification(GradeFinalizedEvent event) {
        logger.debug("Sending GradeFinalizedEvent to {}: {}", "sendNotification-out-0", event);
        streamBridge.send("sendNotification-out-0", event);
    }

    public void checkForAdminNotification(EvaluationSubmittedEvent event) {
        logger.debug("Sending EvaluationSubmittedEventto {}: {}","checkForNotification-out-0", event);
        streamBridge.send("checkForNotification-out-0", event);
    }
}
