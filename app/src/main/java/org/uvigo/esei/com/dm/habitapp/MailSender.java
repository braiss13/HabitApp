package org.uvigo.esei.com.dm.habitapp;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class MailSender {
    private final String senderEmail = "alicianoal1@gmail.com";
    private final String senderPassword ="mdrt ysck fnxg dkrl" ;

    public void sendEmail(String recipientEmail, String subject, String body) {
        // Configuración de propiedades de correo
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Crear sesión
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // Crear mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            // Enviar correo
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

