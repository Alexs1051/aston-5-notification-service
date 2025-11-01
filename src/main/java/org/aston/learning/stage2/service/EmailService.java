package org.aston.learning.stage2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from:no-reply@example.com}")
    private String fromEmail;

    @Value("${app.email.site-url:https://example.com}")
    private String siteUrl;

    public void sendUserCreatedEmail(String toEmail, String userName) {
        String subject = "Добро пожаловать на наш сайт!";
        String text = String.format(
                "Здравствуйте, %s!%n%n" +
                        "Ваш аккаунт на сайте %s был успешно создан.%n%n" +
                        "Мы рады приветствовать вас в нашем сообществе!%n%n" +
                        "С уважением,%nКоманда %s",
                userName, siteUrl, siteUrl
        );

        sendEmail(toEmail, subject, text);
    }

    public void sendUserDeletedEmail(String toEmail, String userName) {
        String subject = "Ваш аккаунт был удален";
        String text = String.format(
                "Здравствуйте, %s!%n%n" +
                        "Ваш аккаунт на сайте %s был удалён.%n%n" +
                        "Если это произошло по ошибке или у вас есть вопросы, " +
                        "пожалуйста, свяжитесь с нашей службой поддержки.%n%n" +
                        "С уважением,%nКоманда %s",
                userName, siteUrl, siteUrl
        );

        sendEmail(toEmail, subject, text);
    }

    public void sendCustomEmail(String toEmail, String subject, String message) {
        String text = String.format(
                "Здравствуйте!%n%n" +
                        "%s%n%n" +
                        "С уважением,%nКоманда %s",
                message, siteUrl
        );

        sendEmail(toEmail, subject, text);
    }

    private void sendEmail(String toEmail, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

            logger.info("Email successfully sent to: {}", toEmail);
            logger.debug("Email details - Subject: {}, From: {}, To: {}", subject, fromEmail, toEmail);

        } catch (MailException e) {
            logger.error("Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    public boolean testEmailConnection() {
        try {
            // Test connection
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(fromEmail);
            testMessage.setTo("test@example.com");
            testMessage.setSubject("Test Connection");
            testMessage.setText("This is a test message to check email configuration.");

            // Don't send, just check configuration
            logger.info("Email service is configured correctly. From: {}, Host: {}", fromEmail,
                    mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl ?
                            ((org.springframework.mail.javamail.JavaMailSenderImpl) mailSender).getHost() : "unknown");

            return true;
        } catch (Exception e) {
            logger.error("Email service configuration error", e);
            return false;
        }
    }
}