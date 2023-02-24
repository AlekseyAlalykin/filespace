package org.filespace.services.threads;

import org.filespace.services.EmailHandler;

import java.io.IOException;

public class EmailThread extends Thread {
    private String email;
    private String subject;
    private String text;

    public EmailThread(String email, String subject, String text){
        this.email = email;
        this.subject = subject;
        this.text = text;
    }

    @Override
    public void run() {
        EmailHandler emailHandler = new EmailHandler();

        try{
            emailHandler.sendMail(email, subject, text);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
