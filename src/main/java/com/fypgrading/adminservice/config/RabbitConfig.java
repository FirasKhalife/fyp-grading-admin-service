//package com.fypgrading.adminservice.config;
//
//import com.fypgrading.adminservice.service.EventHandler;
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitAdmin;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
//import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitConfig {
//
//    /**
//     * Retry Queues Configuration
//     */
//    public static Integer MAX_RETRY_COUNT;
//    public static Integer RETRY_QUEUES_TTL;
//
//    @Value("${spring.rabbitmq.retry-max-count}")
//    public void setRetryCheckNotificationMaxCount(Integer retryCheckNotificationMaxCount) {
//        MAX_RETRY_COUNT = retryCheckNotificationMaxCount;
//    }
//
//    @Value("${spring.rabbitmq.retry-queues-ttl}")
//    public void setRetryCheckNotificationTTL(Integer retryCheckNotificationTTL) {
//        RETRY_QUEUES_TTL = retryCheckNotificationTTL;
//    }
//
//    /**
//     * Check Notification Queue
//     */
//    public static String CHECK_NOTIFICATION_QUEUE_NAME;
//    public static String CHECK_NOTIFICATION_EXCHANGE_NAME;
//    public static String CHECK_NOTIFICATION_ROUTING_KEY;
//
//    @Value("${spring.rabbitmq.check-notification.queue}")
//    public void setCheckNotificationQueueName(String checkNotificationQueueName) {
//        CHECK_NOTIFICATION_QUEUE_NAME = checkNotificationQueueName;
//    }
//
//    @Value("${spring.rabbitmq.check-notification.exchange}")
//    public void setCheckNotificationExchangeName(String checkNotificationExchangeName) {
//        CHECK_NOTIFICATION_EXCHANGE_NAME = checkNotificationExchangeName;
//    }
//
//    @Value("${spring.rabbitmq.check-notification.routing-key}")
//    public void setCheckNotificationRoutingKey(String checkNotificationRoutingKey) {
//        CHECK_NOTIFICATION_ROUTING_KEY = checkNotificationRoutingKey;
//    }
//
//    @Bean
//    public Queue checkNotificationQueue() {
//        return QueueBuilder.durable(CHECK_NOTIFICATION_QUEUE_NAME)
//                .withArgument("x-dead-letter-exchange", RETRY_CHECK_NOTIFICATION_EXCHANGE_NAME)
//                .withArgument("x-dead-letter-routing-key", RETRY_CHECK_NOTIFICATION_ROUTING_KEY)
//                .build();
//    }
//
//    @Bean
//    public TopicExchange checkNotificationExchange() {
//        return new TopicExchange(CHECK_NOTIFICATION_EXCHANGE_NAME);
//    }
//
//    @Bean
//    public Binding bindingCheckNotification(@Qualifier("checkNotificationQueue") Queue queue,
//                                            @Qualifier("checkNotificationExchange") TopicExchange exchange) {
//        return BindingBuilder.bind(queue).to(exchange).with(CHECK_NOTIFICATION_ROUTING_KEY);
//    }
//
//    /**
//     * Retry Check Notification Queue
//     * In case of processing failure in the Check Notification Queue,
//     * the message will be sent to the Retry Check Notification Queue
//     * where it will stay for a certain amount of time (RETRY_CHECK_NOTIFICATION_TTL)
//     * before being sent back to the CheckNotification Queue.
//     */
//    public static String RETRY_CHECK_NOTIFICATION_QUEUE_NAME;
//    public static String RETRY_CHECK_NOTIFICATION_EXCHANGE_NAME;
//    public static String RETRY_CHECK_NOTIFICATION_ROUTING_KEY;
//
//    @Value("${spring.rabbitmq.retry-check-notification.queue}")
//    public void setRetryCheckNotificationQueueName(String retryCheckNotificationQueueName) {
//        RETRY_CHECK_NOTIFICATION_QUEUE_NAME = retryCheckNotificationQueueName;
//    }
//
//    @Value("${spring.rabbitmq.retry-check-notification.exchange}")
//    public void setRetryCheckNotificationExchangeName(String retryCheckNotificationExchangeName) {
//        RETRY_CHECK_NOTIFICATION_EXCHANGE_NAME = retryCheckNotificationExchangeName;
//    }
//
//    @Value("${spring.rabbitmq.retry-check-notification.routing-key}")
//    public void setRetryCheckNotificationRoutingKey(String retryCheckNotificationRoutingKey) {
//        RETRY_CHECK_NOTIFICATION_ROUTING_KEY = retryCheckNotificationRoutingKey;
//    }
//
//    @Bean
//    public Queue retryCheckNotificationQueue() {
//        return QueueBuilder.durable(RETRY_CHECK_NOTIFICATION_QUEUE_NAME)
//                .withArgument("x-message-ttl", RETRY_QUEUES_TTL)
//                .withArgument("x-dead-letter-exchange", CHECK_NOTIFICATION_EXCHANGE_NAME)
//                .withArgument("x-dead-letter-routing-key", CHECK_NOTIFICATION_ROUTING_KEY)
//                .build();
//    }
//
//    @Bean
//    public TopicExchange retryCheckNotificationExchange() {
//        return new TopicExchange(RETRY_CHECK_NOTIFICATION_EXCHANGE_NAME);
//    }
//
//    @Bean
//    public Binding bindingRetryCheckNotification(@Qualifier("retryCheckNotificationQueue") Queue queue,
//                                                 @Qualifier("retryCheckNotificationExchange") TopicExchange exchange) {
//        return BindingBuilder.bind(queue).to(exchange).with(RETRY_CHECK_NOTIFICATION_ROUTING_KEY);
//    }
//
//    /**
//     * Check Notification Listener
//     */
//    @Bean
//    MessageListenerAdapter messageListenerAdapter(EventHandler eventHandler) {
//        MessageListenerAdapter messageListenerAdapter =
//                new MessageListenerAdapter(eventHandler, "onMessage");
//        messageListenerAdapter.setMessageConverter(jackson2JsonMessageConverter());
//        return messageListenerAdapter;
//    }
//
//    @Bean
//    SimpleMessageListenerContainer simpleMessageListenerContainer(ConnectionFactory connectionFactory,
//                                                                  MessageListenerAdapter messageListenerAdapter) {
//        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
//        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
//        simpleMessageListenerContainer.setQueueNames(CHECK_NOTIFICATION_QUEUE_NAME);
//        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
//        return simpleMessageListenerContainer;
//    }
//
//    /**
//     * Notification Queue
//     */
//    public static String NOTIFICATION_QUEUE_NAME;
//    public static String NOTIFICATION_EXCHANGE_NAME;
//    public static String NOTIFICATION_ROUTING_KEY;
//
//    @Value("${spring.rabbitmq.notification.queue}")
//    public void setNotificationQueueName(String notificationQueueName) {
//        NOTIFICATION_QUEUE_NAME = notificationQueueName;
//    }
//
//    @Value("${spring.rabbitmq.notification.exchange}")
//    public void setNotificationExchangeName(String notificationExchangeName) {
//        NOTIFICATION_EXCHANGE_NAME = notificationExchangeName;
//    }
//
//    @Value("${spring.rabbitmq.notification.routing-key}")
//    public void setNotificationRoutingKey(String notificationRoutingKey) {
//        NOTIFICATION_ROUTING_KEY = notificationRoutingKey;
//    }
//
//    @Bean
//    public Queue notificationQueue() {
//        return QueueBuilder.durable(NOTIFICATION_QUEUE_NAME)
//                .withArgument("x-dead-letter-exchange", RETRY_NOTIFICATION_EXCHANGE_NAME)
//                .withArgument("x-dead-letter-routing-key", RETRY_NOTIFICATION_ROUTING_KEY)
//                .build();
//    }
//
//    @Bean
//    public TopicExchange notificationExchange() {
//        return new TopicExchange(NOTIFICATION_EXCHANGE_NAME);
//    }
//
//    @Bean
//    public Binding bindingNotification(@Qualifier("notificationQueue") Queue queue,
//                                       @Qualifier("notificationExchange") TopicExchange exchange) {
//        return BindingBuilder.bind(queue).to(exchange).with(NOTIFICATION_ROUTING_KEY);
//    }
//
//    /**
//     * Retry Notification Queue
//     * In case of processing failure in the Notification Queue, the message will be sent to the Retry Notification Queue where it will stay for a certain amount of time (RETRY_NOTIFICATION_TTL) before being sent back to the Notification Queue
//     */
//    public static String RETRY_NOTIFICATION_QUEUE_NAME;
//    public static String RETRY_NOTIFICATION_EXCHANGE_NAME;
//    public static String RETRY_NOTIFICATION_ROUTING_KEY;
//
//    @Value("${spring.rabbitmq.retry-notification.queue}")
//    public void setRetryNotificationQueueName(String retryNotificationQueueName) {
//        RETRY_NOTIFICATION_QUEUE_NAME = retryNotificationQueueName;
//    }
//
//    @Value("${spring.rabbitmq.retry-notification.exchange}")
//    public void setRetryNotificationExchangeName(String retryNotificationExchangeName) {
//        RETRY_NOTIFICATION_EXCHANGE_NAME = retryNotificationExchangeName;
//    }
//
//    @Value("${spring.rabbitmq.retry-notification.routing-key}")
//    public void setRetryNotificationRoutingKey(String retryNotificationRoutingKey) {
//        RETRY_NOTIFICATION_ROUTING_KEY = retryNotificationRoutingKey;
//    }
//
//    @Bean
//    public Queue retryNotificationQueue() {
//        return QueueBuilder.durable(RETRY_NOTIFICATION_QUEUE_NAME)
//                .withArgument("x-message-ttl", RETRY_QUEUES_TTL)
//                .withArgument("x-dead-letter-exchange", NOTIFICATION_EXCHANGE_NAME)
//                .withArgument("x-dead-letter-routing-key", NOTIFICATION_ROUTING_KEY)
//                .build();
//    }
//
//    @Bean
//    public TopicExchange retryNotificationExchange() {
//        return new TopicExchange(RETRY_NOTIFICATION_EXCHANGE_NAME);
//    }
//
//    @Bean
//    public Binding bindingRetryNotification(@Qualifier("retryNotificationQueue") Queue queue,
//                                            @Qualifier("retryNotificationExchange") TopicExchange exchange) {
//        return BindingBuilder.bind(queue).to(exchange).with(RETRY_NOTIFICATION_ROUTING_KEY);
//    }
//
//
//    /**
//     * Dead Letter Queue
//     * After a certain count of retries, the message will be sent to the Dead Letter Queue
//     */
//    public static String DEAD_QUEUE_NAME;
//    public static String DEAD_EXCHANGE_NAME;
//    public static String DEAD_ROUTING_KEY;
//
//    @Value("${spring.rabbitmq.dead-letter.queue}")
//    public void setDeadQueueName(String deadQueueName) {
//        DEAD_QUEUE_NAME = deadQueueName;
//    }
//
//    @Value("${spring.rabbitmq.dead-letter.exchange}")
//    public void setDeadExchangeName(String deadExchangeName) {
//        DEAD_EXCHANGE_NAME = deadExchangeName;
//    }
//
//    @Value("${spring.rabbitmq.dead-letter.routing-key}")
//    public void setDeadRoutingKey(String deadRoutingKey) {
//        DEAD_ROUTING_KEY = deadRoutingKey;
//    }
//
//    @Bean
//    public Queue deadLetterQueue() {
//        return QueueBuilder.durable(DEAD_QUEUE_NAME).build();
//    }
//
//    @Bean
//    public TopicExchange deadLetterExchange() {
//        return new TopicExchange(DEAD_EXCHANGE_NAME);
//    }
//
//    @Bean
//    public Binding bindingDeadLetter(@Qualifier("deadLetterQueue") Queue queue,
//                                     @Qualifier("deadLetterExchange") TopicExchange exchange) {
//        return BindingBuilder.bind(queue).to(exchange).with(DEAD_ROUTING_KEY);
//    }
//
//    /**
//     * RabbitMQ Config
//     */
//    @Bean
//    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
//        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
//        rabbitAdmin.setAutoStartup(true);
//        return rabbitAdmin;
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
//        return rabbitTemplate;
//    }
//
//    @Bean
//    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//}
