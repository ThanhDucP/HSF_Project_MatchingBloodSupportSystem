package com.chillguy.tiny.blood.service;

import com.chillguy.tiny.blood.dto.*;
import com.chillguy.tiny.blood.entity.Account;
import com.chillguy.tiny.blood.entity.Role;
import com.chillguy.tiny.blood.exception.AccountNotFoundException;
import com.chillguy.tiny.blood.mapper.AccountMapper;
import com.chillguy.tiny.blood.repository.AccountRepository;
import com.chillguy.tiny.blood.repository.RoleRepository;
import com.chillguy.tiny.blood.util.JwtUtil;
import com.chillguy.tiny.blood.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final RoleRepository roleRepository;

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

    public RegisterResponseDto register(RegisterRequestDto requestDto) {

        if(accountRepository.existsByEmail(requestDto.getEmail())){
            throw new BadRequestException("Email đã được sử dụng");
        }

        Account newAccount = new Account();
        newAccount.setAccountId(generateAccountId());
        newAccount.setEmail(requestDto.getEmail());
        newAccount.setUserName(requestDto.getUsername());
        newAccount.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        Role memberRole = roleRepository.findByRole("MEMBER");
        newAccount.setRole(memberRole);
        newAccount.setIsActive(true);
        newAccount.setCreationDate(LocalDate.now());
        accountRepository.save(newAccount);

        RegisterResponseDto responseDto = AccountMapper.accountMapper(newAccount);

        return responseDto;
    }

    private String generateAccountId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "AC-" + timestamp + "-" + random;
    }


    @Transactional
    public void resetPassword(String accountId, ResetPasswordDto requestDto) throws BadRequestException {

        String password = requestDto.getPassword();
        String confirmPassword = requestDto.getConfirmPassword();

        if (!password.equals(confirmPassword))
            throw new BadRequestException("Reset password fail");

        Account accountInDb = accountRepository.findByAccountId(accountId).orElseThrow(
                () -> new AccountNotFoundException("Account not found with id " + accountId)
        );

        accountInDb.setPassword(passwordEncoder.encode(password));
        accountRepository.save(accountInDb);
    }
}
