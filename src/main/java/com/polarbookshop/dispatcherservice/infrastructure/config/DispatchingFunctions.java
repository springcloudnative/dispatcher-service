package com.polarbookshop.dispatcherservice.infrastructure.config;

import com.polarbookshop.dispatcherservice.domain.events.OrderAcceptedEvent;
import com.polarbookshop.dispatcherservice.domain.events.OrderDispatchedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * This class contains all the functions implementing
 * the order packing and labeling business logic.
 */
@Configuration
@Slf4j
public class DispatchingFunctions {

    @Value("${spring.cloud.stream.bindings.packlabel-out-0.destination}")
    private String topic;

    @Bean
    public Function<OrderAcceptedEvent, Message<Long>> pack() {
        return orderAcceptedMessage -> {
            log.info("The order with id {} is packed.", orderAcceptedMessage.getOrderId());
            Message<Long>  message =
                    MessageBuilder.withPayload(orderAcceptedMessage.getOrderId())
                    .setHeader(KafkaHeaders.MESSAGE_KEY, topic)
                    .build();
            return message;
        };
    }

    @Bean
    public Function<Flux<Message<Long>>, Flux<Message<OrderDispatchedEvent>>> label() {
        return orderFlux -> orderFlux.map(orderMessage -> {
            log.info("The order with id {} is labeled.", orderMessage.getPayload().longValue());
            return MessageBuilder.withPayload(new OrderDispatchedEvent(orderMessage.getPayload()))
                    .setHeader(KafkaHeaders.MESSAGE_KEY, orderMessage.getPayload().longValue())
                    .build();
        });
    }
}
