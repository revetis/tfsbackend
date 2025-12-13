package com.example.apps.orders.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderRabbitMQConfig {

    @Value("${app.rabbitmq.order.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.order.queue}")
    private String queueName;

    @Value("${app.rabbitmq.order.routingkey}")
    private String routingKey;

    @Value("${app.rabbitmq.order.dlx}")
    private String dlx;

    @Value("${app.rabbitmq.order.dlq}")
    private String dlq;

    @Value("${app.rabbitmq.order.dl-routingkey}")
    private String dlRoutingKey;

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public DirectExchange orderDeadLetterExchange() {
        return new DirectExchange(dlx);
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", dlRoutingKey)
                .build();
    }

    @Bean
    public Queue orderDeadLetterQueue() {
        return QueueBuilder.durable(dlq)
                .ttl(7 * 24 * 60 * 60 * 1000) // 7 days in milliseconds
                .build();
    }

    @Bean
    public Binding orderBinding(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(routingKey);
    }

    @Bean
    public Binding orderDlBinding(Queue orderDeadLetterQueue, DirectExchange orderDeadLetterExchange) {
        return BindingBuilder.bind(orderDeadLetterQueue).to(orderDeadLetterExchange).with(dlRoutingKey);
    }

    @Bean
    public MessageConverter orderJsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
