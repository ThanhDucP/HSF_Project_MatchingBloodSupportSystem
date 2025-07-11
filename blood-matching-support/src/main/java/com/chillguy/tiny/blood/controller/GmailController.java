package com.chillguy.tiny.blood.controller;

import com.chillguy.tiny.blood.service.NotificationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
public class GmailController {

    private final NotificationManager notificationManager;

    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body
    ) {
        try {
            notificationManager.send("email", to, subject, body);
            return ResponseEntity.ok("Đã gửi Gmail tới: " + to);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/send-multiple")
    public ResponseEntity<?> sendMultipleEmails(
            @RequestBody List<String> emails,
            @RequestParam String subject,
            @RequestParam String body
    ) {
        try {
            notificationManager.sendToMany("email", emails, subject, body);
            return ResponseEntity.ok("Đã gửi Gmail cho " + emails.size() + " người.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi gửi danh sách: " + e.getMessage());
        }
    }

    @PostMapping("/send-dynamic")
    public ResponseEntity<?> sendDynamicEmails(
            @RequestBody List<Object> recipients,
            @RequestParam String subject,
            @RequestParam String body
    ) {
        try {
            notificationManager.sendToDynamicList("email", recipients, subject, body);
            return ResponseEntity.ok("Đã gửi Gmail cho danh sách động " + recipients.size() + " người.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Gửi danh sách động thất bại: " + e.getMessage());
        }
    }

}
