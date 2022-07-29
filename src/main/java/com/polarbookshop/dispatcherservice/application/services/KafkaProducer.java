package com.polarbookshop.dispatcherservice.application.services;

import com.polarbookshop.dispatcherservice.application.dto.OrderAcceptedMessageDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class KafkaProducer {

    private final KafkaTemplate<Object, OrderAcceptedMessageDTO> kafkaTemplate;

    @Value("${spring.cloud.stream.bindings.packlabel-in-0.destination}")
    private String topic;

    public KafkaProducer(KafkaTemplate<Object, OrderAcceptedMessageDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public ListenableFuture<SendResult<Object, OrderAcceptedMessageDTO>> sendMessage(OrderAcceptedMessageDTO message) {
        return this.kafkaTemplate.send(this.topic, message);
    }
}
