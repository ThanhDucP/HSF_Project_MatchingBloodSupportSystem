package com.chillguy.tiny.blood.service;

import com.chillguy.tiny.blood.entity.Account;
import com.chillguy.tiny.blood.repository.Notifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationManager {
    private final List<Notifier> notifiers;

    public void send(String channel, String to, String subject, String body) {
        getNotifier(channel).send(to, subject, body);
    }

    public void sendToMany(String channel, List<String> toList, String subject, String body) {
        getNotifier(channel).sendToMany(toList, subject, body);
    }

    public void sendToDynamicList(String channel, List<Object> recipients, String subject, String body) {
        List<String> emails = extractEmails(recipients);
        getNotifier(channel).sendToMany(emails, subject, body);
    }

    private Notifier getNotifier(String channel) {
        return notifiers.stream()
                .filter(n -> n.getChannel().equalsIgnoreCase(channel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Channel not supported: " + channel));
    }

    private List<String> extractEmails(List<Object> list) {
        List<String> emails = new ArrayList<>();

        for (Object obj : list) {
            if (obj instanceof String email) {
                emails.add(email);
            } else if (obj instanceof Map<?, ?> map && map.get("email") instanceof String emailStr) {
                emails.add(emailStr);
            } else if (obj instanceof Account acc && acc.getEmail() != null) {
                emails.add(acc.getEmail());
            } else {
                System.err.println("⚠️ Unsupported object: " + obj);
            }
        }

        return emails;
    }
}
