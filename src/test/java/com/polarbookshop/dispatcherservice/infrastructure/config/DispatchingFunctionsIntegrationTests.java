package com.polarbookshop.dispatcherservice.infrastructure.config;

import com.polarbookshop.dispatcherservice.application.dto.OrderAcceptedMessageDTO;
import com.polarbookshop.dispatcherservice.application.dto.OrderDispatchedMessageDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;

@FunctionalSpringBootTest
class DispatchingFunctionsIntegrationTests {

    @Autowired
    private FunctionCatalog catalog;

    @Test
    void packAndLabelOrder() {
        Function<OrderAcceptedMessageDTO, Flux<OrderDispatchedMessageDTO>> packAndLabel =
                catalog.lookup(Function.class, "pack|label");
        long orderId = 121L;

        StepVerifier
                .create(packAndLabel.apply(new OrderAcceptedMessageDTO(orderId)))
                .expectNextMatches(dispatchedOrder ->
                        dispatchedOrder.equals(new OrderDispatchedMessageDTO(orderId)))
                .verifyComplete();
    }
}