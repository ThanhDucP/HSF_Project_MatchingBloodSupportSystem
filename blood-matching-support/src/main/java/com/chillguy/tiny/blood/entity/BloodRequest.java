package com.chillguy.tiny.blood.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "blood_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequest {

    @Id
    @Column(name = "id_blood_request", nullable = false)
    private String idBloodRequest;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "Tài khoản yêu cầu không được null")
    private Account account;

    @Column(name = "patient_name", nullable = false)
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

    @Column(name = "status", nullable = false)
    @NotBlank(message = "Trạng thái không được để trống")
    private String status; // e.g: PENDING, MATCHED, FAILED

    @Column(name = "volume", nullable = false)
    @Min(value = 1, message = "Thể tích yêu cầu phải ≥ 1 đơn vị")
    private int volume;

    @Column(name = "request_creation_date", nullable = false)
    @NotNull(message = "Ngày tạo đơn không được null")
    private LocalDate requestCreationDate;

}
