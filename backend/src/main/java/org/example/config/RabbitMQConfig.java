package org.example.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter; // 👈 1. Add this import
import org.springframework.amqp.support.converter.MessageConverter;             // 👈 2. Add this import
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "booking.exchange";
    public static final String ROUTING_KEY = "booking.success";
    public static final String INVOICE_QUEUE = "invoice.queue";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String SMS_QUEUE = "sms.queue";

    // --- All of your existing beans for exchanges, queues, and bindings are correct. ---
    // --- No changes are needed for them. ---
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue invoiceQueue() {
        return new Queue(INVOICE_QUEUE, true);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    public Queue smsQueue() {
        return new Queue(SMS_QUEUE, true);
    }

    @Bean
    public Binding invoiceBinding(Queue invoiceQueue, TopicExchange exchange) {
        return BindingBuilder.bind(invoiceQueue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange exchange) {
        return BindingBuilder.bind(emailQueue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(smsQueue).to(exchange).with(ROUTING_KEY);
    }


    // ✅ 3. Add this new bean to your configuration
    /**
     * This bean tells Spring Boot to use a JSON converter for all messages.
     * It converts Java objects to JSON when sending, and JSON back to Java objects when receiving.
     * This is the modern, safe, and standard way to handle messages.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

