package com.polarbookshop.dispatcherservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polarbookshop.dispatcherservice.domain.events.OrderAcceptedEvent;
import com.polarbookshop.dispatcherservice.domain.events.OrderDispatchedEvent;
import com.polarbookshop.dispatcherservice.application.services.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Configures the test binder
@Import(TestChannelBinderConfiguration.class)
@DirtiesContext
@ActiveProfiles("test")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FunctionsStreamIntegrationTests {

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

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private KafkaAdmin admin;

    // Represents the input binding packlabel-in-0
    @Autowired
    private InputDestination input;

    // Represents the output binding packlabel-out-0
    @Autowired
    private OutputDestination output;

    // Needed to map byte[] to DTOs
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void testCreationOfTopicAtStartup() throws IOException, ExecutionException, InterruptedException {

        AdminClient client = AdminClient.create(admin.getConfigurationProperties());
        Collection<TopicListing> topicList = client.listTopics().listings().get();
        assertNotNull(topicList);
        System.out.println("TOPIC LIST: " + topicList);
        assertEquals(topicList.stream().map(l -> l.name()).collect(Collectors.toList()),
                Arrays.asList("order-accepted","order-dispatched"));
    }

    /**
     * The data flow is based on Message objects (from the org.springframework.messaging
     * package).
     * The framework handles type conversion for you transparently when running the
     * application. However, in this type of test, you need to provide Message objects types explicitly.
     * You can use MessageBuilder to create the input message and the ObjectMapper utility to
     * perform the type conversion from the binary format used for storing message payloads in a
     * broker.
     * The conversion between bytes and DTOs is handled by Spring Cloud  Stream transparently.
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    @Order(2)
    void testPublishOrder() throws IOException, InterruptedException, ExecutionException {

        long orderId = 121;

        Message<OrderAcceptedEvent> inputMessage = MessageBuilder.
                withPayload(new OrderAcceptedEvent(orderId)).build();

        Message<OrderDispatchedEvent> expectedOutputMessage = MessageBuilder.
                withPayload(new OrderDispatchedEvent(orderId)).build();

        // Sends a message to the input channel
        this.input.send(inputMessage);

        // Receives and asserts a message from the output channel
        assertThat(objectMapper.readValue(output.receive().getPayload(), OrderDispatchedEvent.class))
                .isEqualTo(expectedOutputMessage.getPayload());
    }
}
