package com.fypgrading.adminservice.exceptions;

import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.amqp.core.Message;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationException extends RuntimeException {

    private Message queueMessage;

    private Channel channel;

    public NotificationException(String message, Message queueMessage, Channel channel) {
        super(message);
        this.queueMessage = queueMessage;
        this.channel = channel;
    }
}

