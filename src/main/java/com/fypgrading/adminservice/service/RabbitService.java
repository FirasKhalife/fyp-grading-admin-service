package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.config.RabbitConfig;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RabbitService {

    private final Logger logger = LoggerFactory.getLogger(RabbitService.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNack(Message message, Channel channel) {
        MessageProperties props = message.getMessageProperties();
        List<Map<String, ?>> xDeathHeader = props.getXDeathHeader();

        try {
            if (xDeathHeader == null || xDeathHeader.isEmpty() ||
                    (Long) xDeathHeader.get(0).get("count") < RabbitConfig.MAX_RETRY_COUNT) {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                return;
            }
            // if retryCount == RabbitConfig.MAX_RETRY_COUNT, send to dead letter queue
            logger.error("Message sent to dead letter queue: {}", message);
            rabbitTemplate.send("", RabbitConfig.DEAD_QUEUE_NAME, message);
        }
        catch (Exception e) {
            logger.error("Error sending nack: {}", e.getMessage());
        }
    }
}
