package com.polarbookshop.dispatcherservice.application.services;

import com.polarbookshop.dispatcherservice.application.dto.OrderAcceptedMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Service
@ConditionalOnProperty(value = "example.kafka.consumer-enabled", havingValue = "true")
@Slf4j
public class KafkaConsumer {

    private CountDownLatch latch = new CountDownLatch(1);
    private OrderAcceptedMessageDTO payload;

    @KafkaListener(topics = "#{'${spring.cloud.stream.bindings.packlabel-in-0.destination}'.split(',')}")
    public void consume(@Payload OrderAcceptedMessageDTO data,
                        @Headers MessageHeaders messageHeaders,
                        final Acknowledgment acknowledgment
    ) {

        log.info("- - - - - - - - - - - - - - -");
        log.info("received message='{}'", data);
        acknowledgment.acknowledge();

        messageHeaders.keySet().forEach(key -> {
            Object value = messageHeaders.get(key);
            if (key.equals("X-Custom-Header")) {
                log.info("{}: {}", key, new String((byte []) value));
            } else {
                log.info("{}: {}", key, value);
            }
        });

        payload = data;
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }

    public OrderAcceptedMessageDTO getPayload() {
        return payload;
    }
}
