package com.chillguy.tiny.blood.config;

import com.chillguy.tiny.blood.entity.Account;
import com.chillguy.tiny.blood.entity.Role;
import com.chillguy.tiny.blood.repository.AccountRepository;
import com.chillguy.tiny.blood.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STAFF = "STAFF";
    private static final String ROLE_MEMBER = "MEMBER";

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultData() {
        createRoleIfNotExist(ROLE_ADMIN, "Quản trị viên hệ thống");
        createRoleIfNotExist(ROLE_STAFF, "Nhân viên bệnh viện");
        createRoleIfNotExist(ROLE_MEMBER, "Người dùng thường");

        createAccountIfNotExist("admin", "admin@system.local", ROLE_ADMIN);
        createAccountIfNotExist("staff", "staff@hospital.local", ROLE_STAFF);
        createAccountIfNotExist("member", "member@user.local", ROLE_MEMBER);
    }

    private void createRoleIfNotExist(String roleName, String description) {
        if (!roleRepository.existsByRoleIs(roleName)) {
            Role role = new Role();
            role.setRole(roleName);
            role.setDescription(description);
            roleRepository.save(role);
            System.out.printf("✅ Created role: %s%n", roleName);
        }
    }

    private void createAccountIfNotExist(String username, String email, String roleName) {
        if (!accountRepository.existsByUserNameIgnoreCase(username)) {
            String accountId = generateAccountId();
            Account account = new Account();
            account.setAccountId(accountId);
            account.setUserName(username);
            account.setPassword(passwordEncoder.encode("12345678"));
            account.setEmail(email);
            account.setIsActive(true);

            Role role = roleRepository.findByRole(roleName).orElseThrow(
                    () -> new IllegalStateException("Role not found: " + roleName)
            );
            account.setRole(role);

            accountRepository.save(account);
            System.out.printf("✅ Created account: %s / 12345678%n", username);
        } else {
            System.out.printf("ℹ️ Account already exists: %s%n", username);
        }
    }

    private String generateAccountId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "AC-" + timestamp + "-" + random;
    }
}
