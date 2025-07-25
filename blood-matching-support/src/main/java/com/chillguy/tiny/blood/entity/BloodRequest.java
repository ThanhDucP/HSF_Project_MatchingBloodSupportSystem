package com.chillguy.tiny.blood.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "blood_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequest {

    public enum Status {
        PENDING, // DOI STAFF XAC NHAN
        CONFIRMED, // DA HOAN THANH CHU TRINH
        MATCHED, // DA MATCH VS MEMBER KHAC
        CANCELLED // HUY DON
    }

    @Id
    @Column(name = "id_blood_request", nullable = false)
    private String idBloodRequest;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "Tài khoản yêu cầu không được null")
    private Account account;

    @Column(name = "patient_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    @NotBlank(message = "Tên bệnh nhân không được để trống")
    private String patientName;

    @Column(name = "request_date", nullable = false)
    @NotNull(message = "Ngày cần máu không được để trống")
    @FutureOrPresent(message = "Ngày cần máu phải từ hiện tại trở đi")
    private LocalDate requestDate;

    @ManyToOne
    @JoinColumn(name = "blood_code", nullable = false)
    @NotNull(message = "Phải chọn loại máu yêu cầu")
    private Blood bloodCode;

    @Column(name = "is_emergency", nullable = false)
    private boolean isEmergency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "volume", nullable = false)
    @Min(value = 1, message = "Thể tích yêu cầu phải ≥ 1 đơn vị")
    private int volume;

    @Column(name = "request_creation_date", nullable = false)
    @NotNull(message = "Ngày tạo đơn không được null")
    private LocalDate requestCreationDate;

    @Column(name = "confirmed_count")
    private int confirmedCount;

    @Column(name = "is_closed")
    private boolean isClosed;

    // Token → email
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "confirmation_tokens", joinColumns = @JoinColumn(name = "blood_request_id"))
    @MapKeyColumn(name = "token")
    @Column(name = "email")
    private Map<String, String> confirmationTokens = new HashMap<>();

    // Token → thời điểm tạo
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "confirmation_token_created", joinColumns = @JoinColumn(name = "blood_request_id"))
    @MapKeyColumn(name = "token")
    @Column(name = "created_at")
    private Map<String, LocalDateTime> tokenCreatedAt = new HashMap<>();

    // Email đã xác nhận
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "confirmed_emails", joinColumns = @JoinColumn(name = "blood_request_id"))
    @Column(name = "email")
    private Set<String> confirmedAccountIds = new HashSet<>();

    // ====== ID TỰ SINH ======
    @PrePersist
    public void generateId() {
        if (this.idBloodRequest == null) {
            this.idBloodRequest = "BR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
    public boolean isTokenExpired(LocalDateTime createdAt) {
        return createdAt == null || createdAt.plusDays(2).isBefore(LocalDateTime.now());
    }

    public void addConfirmationToken(String token, String email) {
        this.confirmationTokens.put(token, email);
        this.tokenCreatedAt.put(token, LocalDateTime.now());
    }

}
