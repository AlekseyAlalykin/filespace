package org.filespace.services.threads;

import org.filespace.services.util.MailSender;


public class EmailThread extends CustomThread {
    private MailSender emailService;
    private String email;
    private String subject;
    private String text;


    public EmailThread(MailSender emailService, String email, String subject, String text){
        super("Email-Sending-Thread-" + nextThreadNum());
        this.email = email;
        this.subject = subject;
        this.text = text;
        this.emailService = emailService;
    }

    @Override
    public void run() {
        MailSender emailHandler = emailService;
        try{
            emailHandler.sendMail(email, subject, text);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
