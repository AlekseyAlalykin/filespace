package org.filespace.services.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class MailSender {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}") private String sender;

    @Value("${domain-name}")
    private String domainName;

    @Value("${on-registration-subject}")
    private String onRegistrationSubject;

    @Value("${on-deletion-subject}")
    private String onDeletionSubject;

    @Value("${on-email-change-subject}")
    private String onEmailChangeSubject;

    @Value("${on-registration-message}")
    private String onRegistrationMessage;

    @Value("${on-deletion-message}")
    private String onDeletionMessage;

    @Value("${on-email-change-message}")
    private String onEmailChangeMessage;

    public void sendMail(String to, String subject, String text) throws Exception {
        SimpleMailMessage mailMessage
                = new SimpleMailMessage();

        mailMessage.setFrom(sender);
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        javaMailSender.send(mailMessage);

    }

    public String getDomainName() {
        return domainName;
    }

    public String getOnRegistrationSubject() {
        return onRegistrationSubject;
    }

    public String getOnDeletionSubject() {
        return onDeletionSubject;
    }

    public String getOnEmailChangeSubject() {
        return onEmailChangeSubject;
    }

    public String getOnRegistrationMessage() {
        return onRegistrationMessage;
    }

    public String getOnDeletionMessage() {
        return onDeletionMessage;
    }

    public String getOnEmailChangeMessage() {
        return onEmailChangeMessage;
    }

}