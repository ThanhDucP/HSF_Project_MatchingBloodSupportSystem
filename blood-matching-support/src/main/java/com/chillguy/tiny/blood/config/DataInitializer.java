package com.chillguy.tiny.blood.config;

import com.chillguy.tiny.blood.entity.*;
import com.chillguy.tiny.blood.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
    private final ProfileRepository profileRepository;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STAFF = "STAFF";
    private static final String ROLE_MEMBER = "MEMBER";

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultData() {
        initRolesAndAccounts();
        initBloodSamples();
        initProfiles();
    }

    private void initRolesAndAccounts() {
        createRoleIfNotExist(ROLE_ADMIN, "Quản trị viên hệ thống");
        createRoleIfNotExist(ROLE_STAFF, "Nhân viên bệnh viện");
        createRoleIfNotExist(ROLE_MEMBER, "Người dùng thường");

        createAccountIfNotExist("admin", "admin@system.local", ROLE_ADMIN);
        createAccountIfNotExist("staff", "duc49531@gmail.com", ROLE_STAFF);
        createAccountIfNotExist("member", "member@user.local", ROLE_MEMBER);
    }

    private void initProfiles() {
        createProfileIfNotExist("admin", "Nguyễn Văn Admin", "0901234567", LocalDate.of(1985, 5, 15), true);
        createProfileIfNotExist("staff", "Trần Thị Staff", "0912345678", LocalDate.of(1990, 8, 20), false);
        createProfileIfNotExist("member", "Lê Văn Member", "0923456789", LocalDate.of(2000, 2, 10), true);
    }

    private void createProfileIfNotExist(String username, String fullName, String phone, LocalDate dob, boolean gender) {
        Account account = accountRepository.findByUserNameIgnoreCase(username)
                .orElseThrow(() -> new IllegalStateException("Account not found: " + username));

        if (profileRepository.existsByAccount(account)) {
            System.out.printf("Profile already exists for: %s%n", username);
            return;
        }

        Blood sampleBlood = bloodRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No blood sample found"));

        Address address = Address.builder()
                .city("Hồ Chí Minh")
                .district("Quận 1")
                .ward("Phường Bến Nghé")
                .street("Lê Duẩn")
                .latitude(10.7769)
                .longitude(106.7009)
                .build();

        Profile profile = Profile.builder()
                .profileId(generateProfileId())
                .account(account)
                .name(fullName)
                .phone(phone)
                .dob(dob)
                .gender(gender)
                .address(address)
                .numberOfBloodDonation(0L)
                .restDate(LocalDate.now().plusMonths(3))
                .bloodCode(sampleBlood)
                .build();

        profileRepository.save(profile);
        System.out.printf("✅ Created profile for account: %s%n", username);
    }

    private String generateProfileId() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(0, 1000); // 0 to 999
        String randomPart = String.format("%03d", random); // zero-padded to 3 digits
        return "PR-" + datePart + "-" + randomPart;
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
                    .role(roleRepository.findByRole(roleName))
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
                            .bloodCode(generateBloodCode(type, rh)) // ✅ dùng lại hàm mới
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

    private String generateBloodCode(Blood.BloodType type, Blood.RhFactor rh) {
        return type.name() + (rh == Blood.RhFactor.POSITIVE ? "+" : "-");
    }


    private String generateAccountId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "AC-" + timestamp + "-" + random;
    }
}
