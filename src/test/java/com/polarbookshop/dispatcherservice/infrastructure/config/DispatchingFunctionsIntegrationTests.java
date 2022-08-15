package com.polarbookshop.dispatcherservice.infrastructure.config;

import com.polarbookshop.dispatcherservice.domain.events.OrderAcceptedEvent;
import com.polarbookshop.dispatcherservice.domain.events.OrderDispatchedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Testcontainers
@FunctionalSpringBootTest
@Import(TestChannelBinderConfiguration.class)
@ActiveProfiles("test")
class DispatchingFunctionsIntegrationTests {

    @Autowired
    private FunctionCatalog catalog;

    static KafkaContainer kafka;

    static {
        final Map<String, String> env = new LinkedHashMap<>();

        env.put("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "BROKER:PLAINTEXT,PLAINTEXT:SASL_PLAINTEXT");

        env.put("KAFKA_LISTENER_NAME_PLAINTEXT_SASL_ENABLED_MECHANISMS", "PLAIN");

        env.put("KAFKA_LISTENER_NAME_PLAINTEXT_PLAIN_SASL_JAAS_CONFIG", "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                "username=\"admin\" " +
                "password=\"admin-secret\" " +
                "user_admin=\"admin-secret\" " +
                "user_producer=\"producer-secret\" " +
                "user_consumer=\"consumer-secret\";");

        env.put("KAFKA_SASL_JAAS_CONFIG", "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                "username=\"admin\" " +
                "password=\"admin-secret\";");

        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                .withEnv(env)
                .withReuse(true);

        kafka.start();
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void packAndLabelOrder() {
        Function<OrderAcceptedEvent, Flux<Message<OrderDispatchedEvent>>> packAndLabel =
                catalog.lookup(Function.class, "pack|label");
        long orderId = 121L;

        StepVerifier
                .create(packAndLabel.apply(new OrderAcceptedEvent(orderId)))
                .expectNextMatches(dispatchedOrderEvent ->
                        dispatchedOrderEvent.getPayload().getOrderId().equals(orderId))
                .expectComplete();
    }
}