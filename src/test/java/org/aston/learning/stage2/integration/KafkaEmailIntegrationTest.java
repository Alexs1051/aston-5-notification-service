package org.aston.learning.stage2.integration;

import org.aston.learning.stage2.event.UserEvent;
import org.aston.learning.stage2.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class KafkaEmailIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.3.0")
    );

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.properties.spring.json.value.default.type",
                () -> "org.aston.learning.stage2.event.UserEvent");
        registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages",
                () -> "*");
        registry.add("spring.kafka.producer.properties.spring.json.add.type.headers",
                () -> "true");
        registry.add("spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer",
                () -> "org.springframework.kafka.support.serializer.JsonSerializer");
        registry.add("spring.kafka.consumer.key-deserializer",
                () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer",
                () -> "org.springframework.kafka.support.serializer.JsonDeserializer");
    }

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @MockBean
    private EmailService emailService;

    @Test
    void whenUserCreatedEventSent_thenWelcomeEmailShouldBeSent() throws Exception {
        // Arrange
        UserEvent userEvent = new UserEvent("USER_CREATED", "test@example.com", "Test User");

        // Act
        kafkaTemplate.send("user-events", userEvent);

        // Assert
        verify(emailService, timeout(10000).times(1))
                .sendUserCreatedEmail("test@example.com", "Test User");
    }

    @Test
    void whenUserDeletedEventSent_thenDeletionEmailShouldBeSent() throws Exception {
        // Arrange
        UserEvent userEvent = new UserEvent("USER_DELETED", "test@example.com", "Test User");

        // Act
        kafkaTemplate.send("user-events", userEvent);

        // Assert
        verify(emailService, timeout(10000).times(1))
                .sendUserDeletedEmail("test@example.com", "Test User");
    }

    @Test
    void whenUnknownEventTypeSent_thenNoEmailShouldBeSent() throws Exception {
        // Arrange
        UserEvent userEvent = new UserEvent("UNKNOWN_EVENT", "test@example.com", "Test User");

        // Act
        kafkaTemplate.send("user-events", userEvent);

        // Assert
        Thread.sleep(3000);
        verify(emailService, timeout(5000).times(0))
                .sendUserCreatedEmail(anyString(), anyString());
        verify(emailService, timeout(5000).times(0))
                .sendUserDeletedEmail(anyString(), anyString());
    }
}