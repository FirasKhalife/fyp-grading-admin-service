package com.fypgrading.adminservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fypgrading.adminservice.config.RabbitConfig;
import com.fypgrading.adminservice.service.event.GradeFinalizedEvent;
import com.fypgrading.adminservice.service.event.EvaluationSubmittedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Service;

@Service
public class EventDispatcher {

    private final Logger logger = LoggerFactory.getLogger(EventDispatcher.class);
    private final SimpleMessageConverter messageConverter = new SimpleMessageConverter();
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public EventDispatcher(ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAdminNotification(GradeFinalizedEvent event) {
        Message message;
        try {
            message = messageConverter.toMessage(
                    objectMapper.writeValueAsString(event), new MessageProperties()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        logger.info("Sending GradeFinalizedEvent to " + RabbitConfig.NOTIFICATION_QUEUE_NAME + ": " + event);

        rabbitTemplate.send("", RabbitConfig.NOTIFICATION_QUEUE_NAME, message);
    }

    public void checkForAdminNotification(EvaluationSubmittedEvent event) {

        Message message;
        try {
            message = messageConverter.toMessage(
                    objectMapper.writeValueAsString(event), new MessageProperties()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        logger.info("Sending EvaluationSubmittedEvent to " + RabbitConfig.CHECK_NOTIFICATION_QUEUE_NAME + ": " + event);

        rabbitTemplate.send("", RabbitConfig.CHECK_NOTIFICATION_QUEUE_NAME, message);
    }
}
