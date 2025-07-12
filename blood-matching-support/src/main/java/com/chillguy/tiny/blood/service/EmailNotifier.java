package com.chillguy.tiny.blood.service;

import com.chillguy.tiny.blood.repository.Notifier;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailNotifier implements Notifier {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailNotifier(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    @Override
    public void send(String to, String subject, String body) {
        if (!isValidEmail(to)) throw new IllegalArgumentException("Email không hợp lệ: " + to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }


    @Override
    public void sendToMany(List<String> toList, String subject, String body) {
        StringBuilder failedEmails = new StringBuilder();

        for (String to : toList) {
            try {
                send(to, subject, body);
            } catch (Exception e) {
                failedEmails.append("- ").append(to).append(" (").append(e.getMessage()).append(")\n");
            }
        }

        if (failedEmails.length() > 0) {
            throw new RuntimeException("Gửi thất bại với các địa chỉ:\n" + failedEmails);
        }
    }
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // gửi HTML

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email HTML: " + e.getMessage());
        }
    }



    @Override
    public String getChannel() {
        return "email";
    }
}