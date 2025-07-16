package com.chillguy.tiny.blood.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chillguy.tiny.blood.dto.BloodRequestDTO;
import com.chillguy.tiny.blood.dto.BloodRequestResponseDTO;
import com.chillguy.tiny.blood.entity.BloodRequest;
import com.chillguy.tiny.blood.repository.BloodRequestRepository;
import com.chillguy.tiny.blood.service.BloodRequestService;
import com.chillguy.tiny.blood.service.EmailNotifier;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/blood-requests")
public class BloodRequestController {

    private final BloodRequestService service;
    private final BloodRequestRepository requestRepo;
    private final EmailNotifier emailNotifier;

    public BloodRequestController(BloodRequestService service, BloodRequestRepository requestRepo, EmailNotifier emailNotifier) {
        this.service = service;
        this.requestRepo = requestRepo;
        this.emailNotifier = emailNotifier;
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllBloodRequests() {
        try {
            List<BloodRequestResponseDTO> requests = service.getAllRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lấy danh sách yêu cầu máu: " + e.getMessage());
        }
    }

    @PutMapping("/update-status/{requestId}/{status}")
    public ResponseEntity<String> updateStatus(
            @PathVariable String requestId,
            @PathVariable BloodRequest.Status status) {

        try {
            service.updateStatus(requestId, status);
            return ResponseEntity.ok("Cập nhật trạng thái thành công");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Lỗi: " + ex.getMessage());
        }
    }

    @PostMapping("/process")
    public ResponseEntity<?> processRequestWithInventoryCheck(@RequestParam String requestId) {
        try {
            service.processRequestWithInventoryCheck(requestId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Đã xử lý yêu cầu máu với kiểm tra kho máu."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Lỗi xử lý yêu cầu: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody BloodRequestDTO dto) {
        try {
            String accountId = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("DEBUG - AccountId from token: " + accountId);
            return ResponseEntity.ok(service.createRequest(dto, accountId));
        } catch (IllegalArgumentException e) {
            System.out.println("DEBUG - IllegalArgumentException: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("DEBUG - Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }


    @GetMapping("/confirm-by-token")
    public ResponseEntity<?> confirmByToken(
            @RequestParam String requestId,
            @RequestParam String token) {
        try {
            BloodRequest request = requestRepo.findByIdBloodRequest(requestId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn"));

            if (request.isClosed()) {
                return ResponseEntity.badRequest().body("Đơn đã đóng");
            }

            String email = request.getConfirmationTokens().get(token);
            LocalDateTime createdAt = request.getTokenCreatedAt().get(token);

            if (email == null || request.isTokenExpired(createdAt)) {
                request.getConfirmationTokens().remove(token);
                request.getTokenCreatedAt().remove(token);
                requestRepo.save(request);
                return ResponseEntity.badRequest().body("Token không hợp lệ hoặc đã hết hạn");
            }

            if (request.getConfirmedAccountIds().contains(email)) {
                return ResponseEntity.badRequest().body("Bạn đã xác nhận rồi");
            }

            request.getConfirmedAccountIds().add(email);
            request.setConfirmedCount(request.getConfirmedAccountIds().size());

            if (request.getConfirmedCount() >= 3) {
                request.setStatus(BloodRequest.Status.CONFIRMED);
                request.setClosed(true);

                emailNotifier.send(
                        request.getAccount().getEmail(),
                        "Đơn máu đã đủ",
                        "🎉 Đơn máu của bạn đã có đủ người xác nhận hiến máu!"
                );
            }

            // Xóa token sau khi dùng
            request.getConfirmationTokens().remove(token);
            request.getTokenCreatedAt().remove(token);

            requestRepo.save(request);

            return ResponseEntity.ok("Xác nhận thành công! Cảm ơn bạn đã giúp đỡ.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }



}
