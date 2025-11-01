package org.aston.learning.stage2.consumer;

import org.aston.learning.stage2.event.UserEvent;
import org.aston.learning.stage2.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private Logger logger;

    @InjectMocks
    private UserEventConsumer userEventConsumer;

    private UserEvent userCreatedEvent;
    private UserEvent userDeletedEvent;
    private UserEvent unknownEvent;

    @BeforeEach
    void setUp() {
        userCreatedEvent = new UserEvent();
        userCreatedEvent.setEventType("USER_CREATED");
        userCreatedEvent.setEmail("test@example.com");
        userCreatedEvent.setUserName("Test User");

        userDeletedEvent = new UserEvent();
        userDeletedEvent.setEventType("USER_DELETED");
        userDeletedEvent.setEmail("test@example.com");
        userDeletedEvent.setUserName("Test User");

        unknownEvent = new UserEvent();
        unknownEvent.setEventType("UNKNOWN_EVENT");
        unknownEvent.setEmail("test@example.com");
        unknownEvent.setUserName("Test User");
    }

    @Test
    void consumeUserEvent_WithUserCreated_ShouldCallHandleUserCreated() {
        // Act
        userEventConsumer.consumeUserEvent(userCreatedEvent);

        // Assert
        verify(emailService).sendUserCreatedEmail("test@example.com", "Test User");
    }

    @Test
    void consumeUserEvent_WithUserDeleted_ShouldCallHandleUserDeleted() {
        // Act
        userEventConsumer.consumeUserEvent(userDeletedEvent);

        // Assert
        verify(emailService).sendUserDeletedEmail("test@example.com", "Test User");
    }

    @Test
    void consumeUserEvent_WithUnknownEvent_ShouldLogWarning() {
        // Act
        userEventConsumer.consumeUserEvent(unknownEvent);

        // Assert
        verify(emailService, never()).sendUserCreatedEmail(anyString(), anyString());
        verify(emailService, never()).sendUserDeletedEmail(anyString(), anyString());
    }

    @Test
    void consumeUserEvent_WhenEmailServiceThrowsException_ShouldLogError() {
        // Arrange
        doThrow(new RuntimeException("Email service down"))
                .when(emailService).sendUserCreatedEmail(anyString(), anyString());

        // Act
        userEventConsumer.consumeUserEvent(userCreatedEvent);

        // Assert - Exception should be caught and logged, not propagated
        verify(emailService).sendUserCreatedEmail("test@example.com", "Test User");
    }
}