package com.chillguy.tiny.blood.controller;

import com.chillguy.tiny.blood.dto.BloodRequestDTO;
import com.chillguy.tiny.blood.dto.BloodRequestResponseDTO;
import com.chillguy.tiny.blood.dto.ConfirmDonationDTO;
import com.chillguy.tiny.blood.entity.BloodRequest;
import com.chillguy.tiny.blood.service.BloodRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("api/blood-requests")
public class BloodRequestController {

    @Autowired
    private BloodRequestService service;
    @Autowired
    private BloodRequestService bloodRequestService;

    @GetMapping("/getall")
    public ResponseEntity<?> getAllBloodRequests() {
        try {
            List<BloodRequestResponseDTO> requests = service.getAllRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lấy danh sách yêu cầu máu: " + e.getMessage());
        }
    }

    @PutMapping("/update-status/{requestId}")
    public ResponseEntity<String> updateStatus(
            @RequestParam("status") BloodRequest.Status status,
            @PathVariable String requestId) {
        try {
            bloodRequestService.updateStatus(requestId, status);
            return ResponseEntity.ok("Cập nhật trạng thái thành công");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Lỗi: " + ex.getMessage());
        }
    }



    //Tạo đơn xin máu
    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody BloodRequestDTO dto) {
        try {
            String accountId = SecurityContextHolder.getContext().getAuthentication().getName();
            return ResponseEntity.ok(service.createRequest(dto, accountId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }



    //Gửi email đến 20 người hiến máu phù hợp
    @PostMapping("/match")
    public ResponseEntity<?> match(@RequestParam String requestId) {
        try {
            service.matchAndNotify(requestId);
            return ResponseEntity.ok("Đã gửi email đến người hiến phù hợp");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }


    //Xác nhận hiến máu từ email
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@Valid @RequestBody ConfirmDonationDTO dto) {
        try {
            return ResponseEntity.ok(service.confirmDonor(dto));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
}
