package com.chillguy.tiny.blood.service;

import com.chillguy.tiny.blood.dto.LoginRequest;
import com.chillguy.tiny.blood.dto.LoginResponse;
import com.chillguy.tiny.blood.entity.Account;
import com.chillguy.tiny.blood.repository.AccountRepository;
import com.chillguy.tiny.blood.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    public LoginResponse login(LoginRequest loginRequest) {
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            return new LoginResponse(null, "Username hoặc mật khẩu không được để trống", null, null);
        }

        Optional<Account> accountOpt = accountRepository.findByUserName(loginRequest.getUsername());
        if (accountOpt.isEmpty()) {
            accountOpt = accountRepository.findByEmail(loginRequest.getUsername());
        }

        if (accountOpt.isEmpty()) {
            return new LoginResponse(null, "Không tìm thấy người dùng", null, null);
        }

        Account account = accountOpt.get();

        if (Boolean.FALSE.equals(account.getIsActive())) {
            return new LoginResponse(null, "Tài khoản đã bị khóa", null, null);
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())) {
            return new LoginResponse(null, "Sai mật khẩu", null, null);
        }

        String token = jwtUtil.generateToken(account);
        return new LoginResponse(token, "Đăng nhập thành công", account.getAccountId(), account.getRole().getRole());
    }

    public boolean validateToken(String token) {
        try {
            if (tokenBlacklistService.contains(token)) return false;
            String accountId = jwtUtil.extractAccountId(token);
            return jwtUtil.validateToken(token, accountId);
        } catch (Exception e) {
            return false;
        }
    }

    public String logout(String token) {
        try {
            if (tokenBlacklistService.contains(token)) {
                return "Token đã đăng xuất trước đó";
            }

            String accountId = jwtUtil.extractAccountId(token);
            boolean isValid = jwtUtil.validateToken(token, accountId);

            if (!isValid) {
                return "Token không hợp lệ hoặc đã hết hạn";
            }

            tokenBlacklistService.add(token);
            return "Đăng xuất thành công";

        } catch (Exception e) {
            return "Token không hợp lệ hoặc đã hết hạn";
        }
    }

}
