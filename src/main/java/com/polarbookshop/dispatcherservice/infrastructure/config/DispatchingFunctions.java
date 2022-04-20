package com.polarbookshop.dispatcherservice.infrastructure.config;

import com.polarbookshop.dispatcherservice.application.dto.OrderAcceptedMessageDTO;
import com.polarbookshop.dispatcherservice.application.dto.OrderDispatchedMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * This class contains all the functions implementing
 * the order packing and labeling business logic.
 */
@Configuration
@Slf4j
public class DispatchingFunctions {

    @Bean
    public Function<OrderAcceptedMessageDTO, Long> pack() {
        return orderAcceptedMessage -> {
          log.info("The order with id {} is packed.", orderAcceptedMessage.getOrderId());
          return orderAcceptedMessage.getOrderId();
        };
    }

    @Bean
    public Function<Flux<Long>, Flux<OrderDispatchedMessageDTO>> label() {
        return orderFlux -> orderFlux.map(orderId -> {
            log.info("The order with id {} is labeled.", orderId);
            return new OrderDispatchedMessageDTO(orderId);
        });
    }
}
