package org.aston.learning.stage2.consumer;

import org.aston.learning.stage2.event.UserEvent;
import org.aston.learning.stage2.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class UserEventConsumerIntegrationTest {

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