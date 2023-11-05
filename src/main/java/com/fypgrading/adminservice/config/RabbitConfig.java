package com.fypgrading.adminservice.config;

import com.fypgrading.adminservice.service.RabbitService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    /**
     * Notification Queue
     */
    public static String NOTIFICATION_QUEUE_NAME;
    public static String NOTIFICATION_EXCHANGE_NAME;
    public static String NOTIFICATION_ROUTING_KEY;

    @Value("${spring.rabbitmq.notification.queue}")
    public void setNotificationQueueName(String notificationQueueName) {
        NOTIFICATION_QUEUE_NAME = notificationQueueName;
    }

    @Value("${spring.rabbitmq.notification.exchange}")
    public void setNotificationExchangeName(String notificationExchangeName) {
        NOTIFICATION_EXCHANGE_NAME = notificationExchangeName;
    }

    @Value("${spring.rabbitmq.notification.routing-key}")
    public void setNotificationRoutingKey(String notificationRoutingKey) {
        NOTIFICATION_ROUTING_KEY = notificationRoutingKey;
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", RETRY_NOTIFICATION_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", RETRY_NOTIFICATION_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE_NAME);
    }

    @Bean
    public Binding bindingNotification(@Qualifier("notificationQueue") Queue queue,
                                       @Qualifier("notificationExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(NOTIFICATION_ROUTING_KEY);
    }

    /**
     * RabbitMQ Listener
     */
    @Bean
    MessageListenerAdapter messageListenerAdapter(RabbitService rabbitService) {
        MessageListenerAdapter messageListenerAdapter =
                new MessageListenerAdapter(rabbitService, "onMessage");
        messageListenerAdapter.setMessageConverter(jackson2JsonMessageConverter());
        return messageListenerAdapter;
    }

    @Bean
    SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory,
                                                                  MessageListenerAdapter messageListenerAdapter) {
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
        simpleMessageListenerContainer.setQueueNames(NOTIFICATION_QUEUE_NAME);
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
        return simpleMessageListenerContainer;
    }

    /**
     * RabbitMQ Config
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
