package com.chillguy.tiny.blood.config;

import com.chillguy.tiny.blood.entity.*;
import com.chillguy.tiny.blood.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final BloodRepository bloodRepository;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STAFF = "STAFF";
    private static final String ROLE_MEMBER = "MEMBER";

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultData() {
        initRolesAndAccounts();
        initBloodSamples();
    }

    private void initRolesAndAccounts() {
        createRoleIfNotExist(ROLE_ADMIN, "Quản trị viên hệ thống");
        createRoleIfNotExist(ROLE_STAFF, "Nhân viên bệnh viện");
        createRoleIfNotExist(ROLE_MEMBER, "Người dùng thường");

        createAccountIfNotExist("admin", "admin@system.local", ROLE_ADMIN);
        createAccountIfNotExist("staff", "staff@hospital.local", ROLE_STAFF);
        createAccountIfNotExist("member", "member@user.local", ROLE_MEMBER);
    }

    private void createRoleIfNotExist(String roleName, String description) {
        if (!roleRepository.existsByRoleIs(roleName)) {
            Role role = Role.builder()
                    .role(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
            System.out.printf("✅ Created role: %s%n", roleName);
        }
    }

    private void createAccountIfNotExist(String username, String email, String roleName) {
        if (!accountRepository.existsByUserNameIgnoreCase(username)) {
            String accountId = generateAccountId();
            Account account = Account.builder()
                    .accountId(accountId)
                    .userName(username)
                    .password(passwordEncoder.encode("12345678"))
                    .email(email)
                    .isActive(true)
                    .role(roleRepository.findByRole(roleName)
                            .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName)))
                    .build();

            accountRepository.save(account);
            System.out.printf("✅ Created account: %s / 12345678%n", username);
        } else {
            System.out.printf("ℹ️ Account already exists: %s%n", username);
        }
    }


    private void initBloodSamples() {
        if (bloodRepository.count() > 0) return;

        for (Blood.BloodType type : Blood.BloodType.values()) {
            for (Blood.RhFactor rh : Blood.RhFactor.values()) {
                for (Blood.ComponentType component : Blood.ComponentType.values()) {
                    Blood blood = Blood.builder()
                            .bloodCode(generateBloodCode(type, rh, component))
                            .bloodType(type)
                            .rh(rh)
                            .componentType(component)
                            .isRareBlood(isRare(type, rh))
                            .quantity(0)
                            .build();

                    bloodRepository.save(blood);
                }
            }
        }

        System.out.println("✅ Blood samples initialized.");
    }

    private boolean isRare(Blood.BloodType type, Blood.RhFactor rh) {
        String code = type.name() + (rh == Blood.RhFactor.POSITIVE ? "+" : "-");

        return List.of("AB-", "B-", "A-", "O-").contains(code);
    }



    private String generateBloodCode(Blood.BloodType type, Blood.RhFactor rh, Blood.ComponentType comp) {
        return type.name() + "_" + (rh == Blood.RhFactor.POSITIVE ? "POS" : "NEG") + "_" + comp.name();
    }

    private String generateAccountId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "AC-" + timestamp + "-" + random;
    }
}
