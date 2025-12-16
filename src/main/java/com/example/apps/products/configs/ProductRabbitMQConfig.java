package com.example.apps.products.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductRabbitMQConfig {

    @Value("${app.rabbitmq.product.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.product.queue}")
    private String queueName;

    @Value("${app.rabbitmq.product.routingkey}")
    private String routingKey;

    @Value("${app.rabbitmq.product.dlx}")
    private String dlx;

    @Value("${app.rabbitmq.product.dlq}")
    private String dlq;

    @Value("${app.rabbitmq.product.dl-routingkey}")
    private String dlRoutingKey;

    @Bean
    public DirectExchange productExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlx);
    }

    @Bean
    public Queue productQueue() {
        return org.springframework.amqp.core.QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", dlRoutingKey)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(dlq, true);
    }

    @Bean
    public Binding binding(@Qualifier("productQueue") Queue queue,
            @Qualifier("productExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public Binding dlBinding(@Qualifier("deadLetterQueue") Queue deadLetterQueue,
            @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(dlRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}