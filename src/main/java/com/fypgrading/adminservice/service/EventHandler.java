package com.fypgrading.adminservice.service;

import com.fypgrading.adminservice.config.RabbitConfig;
import com.fypgrading.adminservice.exceptions.RabbitException;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class EventHandler implements ChannelAwareMessageListener {

    private final Logger logger = LoggerFactory.getLogger(EventHandler.class);
    private final RabbitTemplate rabbitTemplate;

    public EventHandler(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void onMessage(Message message, Channel channel) {
        try {
            logger.info("Received message: " + message);

            throw new RabbitException("Exception raised!", message, channel);
        } catch (RabbitException ex) {
            sendNack(ex);
        }
    }

    private void sendNack(RabbitException ex) {
        Message message = ex.getQueueMessage();
        MessageProperties props = message.getMessageProperties();
        List<Map<String, ?>> xDeathHeader = props.getXDeathHeader();

        try {
            if (xDeathHeader == null || xDeathHeader.isEmpty() ||
                    (Long) xDeathHeader.get(0).get("count") < RabbitConfig.RETRY_NOTIFICATION_MAX_COUNT) {
                ex.getChannel().basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                return;
            }
            // if retryCount == RabbitConfig.RETRY_NOTIFICATION_MAX_COUNT, send to dead letter queue
            logger.warn("Message sent to dead letter queue: " + message);
            rabbitTemplate.send("", RabbitConfig.DEAD_QUEUE_NAME, message);
        }
        catch (IOException e) {
            logger.error("Error sending nack: " + e.getMessage());
        }
    }
}

