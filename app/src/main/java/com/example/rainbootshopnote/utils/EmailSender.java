package com.example.rainbootshopnote.utils;

import java.io.File;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailSender {

    // E-posta ile dosya ekli şekilde gönderim yapan metod
    public static void sendEmailWithAttachment(final String toEmail, final String subject, final String body, final File attachment) {

        final String fromEmail = "web.info.1379@gmail.com"; // Gönderenin email adresi
        final String password = "ynxw cmnf xqtf mwrv"; // Uygulama şifresi

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP Host
        props.put("mail.smtp.port", "465"); // SSL Port
        props.put("mail.smtp.auth", "true"); // Enable authentication
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // SSL Factory Class

        // E-posta doğrulaması
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // E-posta oluştur
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // E-posta gövdesi
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);

            // Ek dosya
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachment);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(attachment.getName());

            // E-posta içeriğini çoklu parça halinde birleştir
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentBodyPart);

            // Mesaja parçaları ekle
            message.setContent(multipart);

            // E-postayı gönder
            Transport.send(message);
            System.out.println("E-posta başarıyla gönderildi");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
