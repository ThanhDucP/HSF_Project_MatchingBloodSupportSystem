package com.chillguy.tiny.blood.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "blood_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequest {

    public enum Status {
        PENDING,
        CONFIRMED,
        MATCHED,
        CANCELLED,
        COMPLETED
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


    @PrePersist
    public void generateId() {
        if (this.idBloodRequest == null) {
            this.idBloodRequest = "BR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}
