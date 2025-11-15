package org.aston.learning.stage2.consumer;

import org.aston.learning.stage2.event.UserEvent;
import org.aston.learning.stage2.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=optional:file:.env[.properties]",
        "eureka.client.enabled=false"
})
class UserEventConsumerIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.3.0")
    );

    static {
        kafka.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.key-deserializer",
                () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer",
                () -> "org.springframework.kafka.support.serializer.JsonDeserializer");
        registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages",
                () -> "*");
    }

    @Autowired
    private UserEventConsumer userEventConsumer;

    @MockBean
    private EmailService emailService;

    @Test
    void whenUserCreatedEventConsumed_thenEmailServiceShouldBeCalled() {
        // Arrange
        UserEvent userEvent = new UserEvent("USER_CREATED", "test@example.com", "Test User");

        // Act
        userEventConsumer.consumeUserEvent(userEvent);

        // Assert
        verify(emailService, times(1))
                .sendUserCreatedEmail("test@example.com", "Test User");
    }

    @Test
    void whenUserDeletedEventConsumed_thenEmailServiceShouldBeCalled() {
        // Arrange
        UserEvent userEvent = new UserEvent("USER_DELETED", "test@example.com", "Test User");

        // Act
        userEventConsumer.consumeUserEvent(userEvent);

        // Assert
        verify(emailService, times(1))
                .sendUserDeletedEmail("test@example.com", "Test User");
    }

    @Test
    void whenUnknownEventConsumed_thenEmailServiceShouldNotBeCalled() {
        // Arrange
        UserEvent userEvent = new UserEvent("UNKNOWN_EVENT", "test@example.com", "Test User");

        // Act
        userEventConsumer.consumeUserEvent(userEvent);

        // Assert
        verify(emailService, never())
                .sendUserCreatedEmail(anyString(), anyString());
        verify(emailService, never())
                .sendUserDeletedEmail(anyString(), anyString());
    }
}