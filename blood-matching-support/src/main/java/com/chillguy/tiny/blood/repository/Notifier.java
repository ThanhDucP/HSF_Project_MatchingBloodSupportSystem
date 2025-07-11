package com.chillguy.tiny.blood.repository;

import java.util.List;

public interface Notifier {
    void send(String to, String subject, String body);
    void sendToMany(List<String> toList, String subject, String body);
    String getChannel();
}

