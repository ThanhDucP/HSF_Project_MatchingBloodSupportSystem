package com.chillguy.tiny.blood.controller;

import com.chillguy.tiny.blood.dto.*;
import com.chillguy.tiny.blood.dto.response.ApiResponse;
import com.chillguy.tiny.blood.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.chillguy.tiny.blood.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ==== Đăng nhập ====
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);

        return (response.getToken() != null)
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ==== Đăng xuất ====
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Thiếu hoặc sai định dạng token");
        }

        String token = authHeader.substring(7);
        String message = authService.logout(token);
        return ResponseEntity.ok(message);
    }


    // ==== Kiểm tra token còn hiệu lực ====
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(false);
        }

        String token = authHeader.substring(7);
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> register(@RequestBody @Valid RegisterRequestDto requestDto, BindingResult bindingResult) {
        
        RegisterResponseDto responseDto = authService.register(requestDto);
        return ResponseEntity.ok(ApiResponse.<RegisterResponseDto>builder()
                .code(200)
                .message("Registered successfully!")
                .result(responseDto)
                .build());
    }


    @PreAuthorize("hasRole('ADMIN') or (hasRole('MEMBER') and #accountId == authentication.principal.accountId)")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestParam String accountId, @RequestBody ResetPasswordDto requestDto) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            authService.resetPassword(accountId, requestDto);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .code(200)
                    .message("Reset password successfully!")
                    .build());
        } catch (BadRequestException e) {
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .code(400)
                    .message("Reset password failed!")
                    .build());
        }
    }
}
