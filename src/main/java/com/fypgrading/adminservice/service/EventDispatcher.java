package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.config.RabbitConfig;
import com.fypgrading.adminservice.service.enums.AssessmentEnum;
import com.fypgrading.adminservice.service.event.AllReviewersSubmittedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Service;

@Service
public class EventDispatcher {

    private final Logger logger = LoggerFactory.getLogger(EventDispatcher.class);
    private final SimpleMessageConverter converter = new SimpleMessageConverter();
    private final RabbitTemplate rabbitTemplate;

    public EventDispatcher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAdminNotification(int teamId, AssessmentEnum assessment) {
        AllReviewersSubmittedEvent event = new AllReviewersSubmittedEvent(teamId, assessment);
//        Message message = converter.toMessage(event, new MessageProperties());

        logger.info("Sending EvaluationSubmittedEvent to " + RabbitConfig.NOTIFICATION_QUEUE_NAME + ": " + event);

        rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATION_ROUTING_KEY, event);

//        rabbitTemplate.send(
//                "",
//                RabbitConfig.NOTIFICATION_ROUTING_KEY,
//                message);
    }
}
