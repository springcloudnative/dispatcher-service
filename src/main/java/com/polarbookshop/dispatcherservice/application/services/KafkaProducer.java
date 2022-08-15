package com.polarbookshop.dispatcherservice.application.services;

import com.polarbookshop.dispatcherservice.domain.events.OrderAcceptedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class KafkaProducer {

    private final KafkaTemplate<Object, OrderAcceptedEvent> kafkaTemplate;

    @Value("${spring.cloud.stream.bindings.packlabel-in-0.destination}")
    private String topic;

    public KafkaProducer(KafkaTemplate<Object, OrderAcceptedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public ListenableFuture<SendResult<Object, OrderAcceptedEvent>> sendMessage(OrderAcceptedEvent message) {
        return this.kafkaTemplate.send(this.topic, message);
    }
}
