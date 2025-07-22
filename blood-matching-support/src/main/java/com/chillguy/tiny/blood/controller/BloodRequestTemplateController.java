package com.chillguy.tiny.blood.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.chillguy.tiny.blood.dto.BloodRequestResponseDTO;
import com.chillguy.tiny.blood.service.BloodRequestService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BloodRequestTemplateController {

    private final BloodRequestService bloodRequestService;

    public BloodRequestTemplateController(BloodRequestService bloodRequestService) {
        this.bloodRequestService = bloodRequestService;
    }

    @GetMapping("/blood-requests")
    public String listBloodRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        try {
            // Lấy dữ liệu thực từ service
            List<BloodRequestResponseDTO> bloodRequests = bloodRequestService.getAllRequests();
            model.addAttribute("bloodRequests", bloodRequests);
            
            // Tính thống kê thực tế
            Map<String, Integer> stats = calculateStats(bloodRequests);
            model.addAttribute("stats", stats);
            
        } catch (Exception e) {
            // Nếu có lỗi, dùng dữ liệu trống
            model.addAttribute("bloodRequests", List.of());
            model.addAttribute("stats", getEmptyStats());
        }
        
        // Add filter parameters
        model.addAttribute("currentStatus", status != null ? status : "");
        model.addAttribute("currentBloodType", bloodType != null ? bloodType : "");
        model.addAttribute("currentKeyword", keyword != null ? keyword : "");
        
        // Add pagination data
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 1);
        
        return "blood-request/list";
    }

    private Map<String, Integer> calculateStats(List<BloodRequestResponseDTO> requests) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("pending", 0);
        stats.put("matched", 0);
        stats.put("confirmed", 0);
        stats.put("completed", 0);
        
        for (BloodRequestResponseDTO request : requests) {
            String status = request.getStatus().toString().toLowerCase();
            stats.put(status, stats.getOrDefault(status, 0) + 1);
        }
        
        return stats;
    }

    private Map<String, Integer> getEmptyStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("pending", 0);
        stats.put("matched", 0);
        stats.put("confirmed", 0);
        stats.put("completed", 0);
        return stats;
    }

    @GetMapping("/blood-requests/create")
    public String createBloodRequest() {
        return "blood-request/create";
    }

    @GetMapping("/blood-requests/list")
    public String listBloodRequestsAlias(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        // Redirect to the main listing page with same parameters
        return listBloodRequests(status, bloodType, keyword, page, model);
    }
}
