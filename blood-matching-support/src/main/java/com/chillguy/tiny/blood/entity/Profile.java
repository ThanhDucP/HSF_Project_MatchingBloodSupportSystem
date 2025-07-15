package com.chillguy.tiny.blood.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
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
    private Account account;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dob;

    @Column(name = "gender", nullable = false)
    private Boolean gender; // true = Nam, false = Nữ (nếu cần enum thì nói)

    @Embedded
    @Valid
    private Address address;

    @Column(name = "number_of_blood_donation")
    private Long numberOfBloodDonation;

    @ManyToOne
    @JoinColumn(name = "blood_code")
    private Blood bloodCode;

    @Column(name = "rest_date")
    private LocalDate restDate; // ngày được hiến máu tiếp theo
}
