package com.sms.grade.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for grade-service.
 * Configures exchanges, queues, and bindings for grade events.
 */
@Configuration
public class RabbitMQConfig {

    // Exchange name
    public static final String GRADE_EXCHANGE = "grade.exchange";

    // Queue names
    public static final String GRADE_ENTERED_QUEUE = "grade.entered.queue";
    public static final String GRADE_UPDATED_QUEUE = "grade.updated.queue";
    public static final String GRADE_DELETED_QUEUE = "grade.deleted.queue";
    public static final String AVERAGE_CALCULATED_QUEUE = "grade.average.calculated.queue";

    // Routing keys
    public static final String GRADE_ENTERED_ROUTING_KEY = "grade.entered";
    public static final String GRADE_UPDATED_ROUTING_KEY = "grade.updated";
    public static final String GRADE_DELETED_ROUTING_KEY = "grade.deleted";
    public static final String AVERAGE_CALCULATED_ROUTING_KEY = "grade.average.calculated";

    @Bean
    public TopicExchange gradeExchange() {
        return new TopicExchange(GRADE_EXCHANGE);
    }

    @Bean
    public Queue gradeEnteredQueue() {
        return QueueBuilder.durable(GRADE_ENTERED_QUEUE).build();
    }

    @Bean
    public Queue gradeUpdatedQueue() {
        return QueueBuilder.durable(GRADE_UPDATED_QUEUE).build();
    }

    @Bean
    public Queue gradeDeletedQueue() {
        return QueueBuilder.durable(GRADE_DELETED_QUEUE).build();
    }

    @Bean
    public Queue averageCalculatedQueue() {
        return QueueBuilder.durable(AVERAGE_CALCULATED_QUEUE).build();
    }

    @Bean
    public Binding gradeEnteredBinding(Queue gradeEnteredQueue, TopicExchange gradeExchange) {
        return BindingBuilder.bind(gradeEnteredQueue).to(gradeExchange).with(GRADE_ENTERED_ROUTING_KEY);
    }

    @Bean
    public Binding gradeUpdatedBinding(Queue gradeUpdatedQueue, TopicExchange gradeExchange) {
        return BindingBuilder.bind(gradeUpdatedQueue).to(gradeExchange).with(GRADE_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding gradeDeletedBinding(Queue gradeDeletedQueue, TopicExchange gradeExchange) {
        return BindingBuilder.bind(gradeDeletedQueue).to(gradeExchange).with(GRADE_DELETED_ROUTING_KEY);
    }

    @Bean
    public Binding averageCalculatedBinding(Queue averageCalculatedQueue, TopicExchange gradeExchange) {
        return BindingBuilder.bind(averageCalculatedQueue).to(gradeExchange).with(AVERAGE_CALCULATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
