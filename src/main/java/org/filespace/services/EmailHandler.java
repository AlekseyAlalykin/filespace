package org.filespace.services;

import org.springframework.util.ResourceUtils;
import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EmailHandler {

    static private String smtpPropertiesPath =  "classpath:smtp.properties";
    static private String domainPropertiesPath = "classpath:domain.properties";
    static private String emailMessagePropertiesPath = "classpath:email-message.properties";

    static private String emailLogin;
    static private String password;
    static private String smtpServer;
    static private int smtpServerPort;

    static public String domainName;

    static public String onRegistrationSubject;
    static public String onDeletionSubject;
    static public String onEmailChangeSubject;
    static public String onRegistrationMessage;
    static public String onDeletionMessage;
    static public String onEmailChangeMessage;

    static {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(ResourceUtils.getFile(smtpPropertiesPath)));
            emailLogin = props.getProperty("login");
            password = props.getProperty("password");
            smtpServer = props.getProperty("server");
            smtpServerPort = Integer.parseInt(props.getProperty("port"));

            props.load(new FileInputStream(ResourceUtils.getFile(domainPropertiesPath)));
            domainName = props.getProperty("domain-name");

            props.load(new FileInputStream(ResourceUtils.getFile(emailMessagePropertiesPath)));
            onRegistrationSubject = props.getProperty("on-registration-subject");
            onDeletionSubject = props.getProperty("on-deletion-subject");
            onEmailChangeSubject = props.getProperty("on-email-change-subject");

            onRegistrationMessage = props.getProperty("on-registration-message");
            onDeletionMessage = props.getProperty("on-deletion-message");
            onEmailChangeMessage = props.getProperty("on-email-change-message");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMail(String to, String subject, String text) throws IOException {

        Properties properties = new Properties();

        properties.put("mail.transport.protocol","smtp");
        properties.put("mail.smtp.auth","true");
        properties.put("mail.smtp.ssl.enable","true");
        properties.put("mail.debug","true");
        properties.put("mail.smtp.port",smtpServerPort);
        properties.put("mail.smtp.host",smtpServer);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailLogin,password);
            }
        });

        MimeMessage msg = new MimeMessage(session);

        try {
            msg.addRecipients(Message.RecipientType.TO,to);
            msg.setFrom(emailLogin);
            msg.setSubject(subject);

            Multipart emailContent = new MimeMultipart();

            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(text);

            emailContent.addBodyPart(textBodyPart);

            msg.setContent(emailContent);

            Transport.send(msg);
        } catch (MessagingException e){

        }

    }
}