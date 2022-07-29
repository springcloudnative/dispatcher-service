package com.polarbookshop.dispatcherservice.application.services;

import com.polarbookshop.dispatcherservice.application.dto.OrderAcceptedMessageDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
    private String payload;

//    @KafkaListener(topics = {"order-accepted"}, groupId = "dispatcher-service")
    @KafkaListener(topics = "#{'${spring.cloud.stream.bindings.packlabel-in-0.destination}'.split(',')}")
    public void consume(ConsumerRecord<?, ?> consumerRecord,
                        @Headers MessageHeaders headers,
                        final Acknowledgment acknowledgment
    ) {
//        log.info(String.format("#### -> Consumed message -> TIMESTAMP: %d\n%s\noffset: %d\nkey: %s\npartition: %d\ntopic: %s", ts, message, offset, key, partition, topic));
        log.info("received payload='{}'", consumerRecord.toString());
        acknowledgment.acknowledge();
        payload = consumerRecord.toString();
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }

    public String getPayload() {
        return payload;
    }
}
