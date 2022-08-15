package com.polarbookshop.dispatcherservice.application.services;

import com.polarbookshop.dispatcherservice.domain.events.OrderAcceptedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class SendMessageTask {

    private final KafkaProducer kafkaProducer;
    long orderId = 120;

    public SendMessageTask(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    // run every 50 sec
    @Scheduled(fixedRateString = "50000")
    public void send() throws ExecutionException, InterruptedException {
        ListenableFuture<SendResult<Object, OrderAcceptedEvent>> listenableFuture =
                this.kafkaProducer.sendMessage(new OrderAcceptedEvent(++this.orderId));

        SendResult<Object, OrderAcceptedEvent> result = listenableFuture.get();

        log.info(String.format("Produced:\ntopic: %s\noffset: %d\npartition: %d\nvalue size: %d", result.getRecordMetadata().topic(),
                result.getRecordMetadata().offset(),
                result.getRecordMetadata().partition(), result.getRecordMetadata().serializedValueSize()));
    }
}
