package org.aston.learning.stage2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    private final String testEmail = "test@example.com";
    private final String testUserName = "Test User";

    @BeforeEach
    void setUp() {
        setField(emailService, "fromEmail", "noreply@test.com");
        setField(emailService, "siteUrl", "https://test.com");
    }

    private void setField(EmailService service, String fieldName, String value) {
        try {
            var field = EmailService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sendUserCreatedEmail_ShouldSendWelcomeEmailWithCorrectContent() {
        // Act
        emailService.sendUserCreatedEmail(testEmail, testUserName);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertAll(
                () -> assertEquals(testEmail, sentMessage.getTo()[0]),
                () -> assertEquals("noreply@test.com", sentMessage.getFrom()),
                () -> assertEquals("Добро пожаловать на наш сайт!", sentMessage.getSubject()),
                () -> assertTrue(sentMessage.getText().contains(testUserName)),
                () -> assertTrue(sentMessage.getText().contains("https://test.com")),
                () -> assertTrue(sentMessage.getText().contains("Ваш аккаунт на сайте")),
                () -> assertTrue(sentMessage.getText().contains("был успешно создан"))
        );
    }

    @Test
    void sendUserDeletedEmail_ShouldSendDeletionEmailWithCorrectContent() {
        // Act
        emailService.sendUserDeletedEmail(testEmail, testUserName);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertAll(
                () -> assertEquals(testEmail, sentMessage.getTo()[0]),
                () -> assertEquals("noreply@test.com", sentMessage.getFrom()),
                () -> assertEquals("Ваш аккаунт был удален", sentMessage.getSubject()),
                () -> assertTrue(sentMessage.getText().contains(testUserName)),
                () -> assertTrue(sentMessage.getText().contains("https://test.com")),
                () -> assertTrue(sentMessage.getText().contains("Ваш аккаунт на сайте")),
                () -> assertTrue(sentMessage.getText().contains("был удалён")),
                () -> assertTrue(sentMessage.getText().contains("службой поддержки"))
        );
    }

    @Test
    void sendCustomEmail_ShouldSendCustomMessageWithCorrectFormat() {
        // Arrange
        String subject = "Test Subject";
        String message = "Test Message Content";

        // Act
        emailService.sendCustomEmail(testEmail, subject, message);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertAll(
                () -> assertEquals(testEmail, sentMessage.getTo()[0]),
                () -> assertEquals("noreply@test.com", sentMessage.getFrom()),
                () -> assertEquals(subject, sentMessage.getSubject()),
                () -> assertTrue(sentMessage.getText().contains(message)),
                () -> assertTrue(sentMessage.getText().contains("Здравствуйте!")),
                () -> assertTrue(sentMessage.getText().contains("Команда https://test.com"))
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void sendUserCreatedEmail_WithInvalidUserName_ShouldStillSendEmail(String invalidUserName) {
        // Act
        emailService.sendUserCreatedEmail(testEmail, invalidUserName);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals(testEmail, sentMessage.getTo()[0]);
        // Check, that email was send even with invalid name
    }

    @ParameterizedTest
    @CsvSource({
            "user@test.com, User Name",
            "another@example.org, Another User",
            "test+tag@domain.com, Test User With Tag"
    })
    void sendUserCreatedEmail_WithDifferentEmailsAndNames_ShouldFormatCorrectly(String email, String name) {
        // Act
        emailService.sendUserCreatedEmail(email, name);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertAll(
                () -> assertEquals(email, sentMessage.getTo()[0]),
                () -> assertTrue(sentMessage.getText().contains(name)),
                () -> assertTrue(sentMessage.getText().contains("https://test.com"))
        );
    }

    @Test
    void sendEmail_WhenMailAuthenticationException_ShouldThrowRuntimeException() {
        // Arrange
        doThrow(new MailAuthenticationException("Authentication failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendUserCreatedEmail(testEmail, testUserName));

        assertTrue(exception.getMessage().contains("Failed to send email"));
        assertTrue(exception.getMessage().contains("Authentication failed"));
    }

    @Test
    void sendEmail_WhenMailSendException_ShouldThrowRuntimeException() {
        // Arrange
        doThrow(new MailSendException("Could not send mail"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendUserCreatedEmail(testEmail, testUserName));

        assertTrue(exception.getMessage().contains("Failed to send email"));
        assertTrue(exception.getMessage().contains("Could not send mail"));
    }

    @Test
    void testEmailConnection_WhenConfigurationIsValid_ShouldReturnTrue() {
        // Act
        boolean result = emailService.testEmailConnection();

        // Assert
        assertTrue(result);
        // Check, if there was no send (just check config)
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}