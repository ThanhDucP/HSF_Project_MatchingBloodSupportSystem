package com.chillguy.tiny.blood.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "Tài khoản không được null")
    private Account account;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Tên không được để trống")
    private String name;

    @Column(name = "phone", nullable = false)
    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải từ 10–11 chữ số")
    private String phone;

    @Column(name = "date_of_birth", nullable = false)
    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là trong quá khứ")
    private LocalDate dob;

    @Column(name = "gender", nullable = false)
    private boolean gender; // true = Nam, false = Nữ (nếu cần enum thì nói)

    @Embedded
    @Valid
    private Address address;

    @Column(name = "number_of_blood_donation", nullable = false)
    @Min(value = 0, message = "Số lần hiến máu phải ≥ 0")
    private long numberOfBloodDonation;

    @ManyToOne
    @JoinColumn(name = "blood_code", nullable = false)
    @NotNull(message = "Phải chọn nhóm máu")
    private Blood bloodCode;

    @Column(name = "rest_date")
    private LocalDate restDate; // ngày được hiến máu tiếp theo
}
