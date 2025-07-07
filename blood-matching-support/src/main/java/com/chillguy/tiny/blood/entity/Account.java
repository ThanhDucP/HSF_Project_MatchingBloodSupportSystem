package com.chillguy.tiny.blood.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "id")
    private String accountId;

    @Column(name = "username", unique = true, nullable = false)
    @NotBlank(message = "Username không được để trống")
    private String userName;

    @Column(name = "email", unique = true, nullable = false)
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @Column(name = "password")
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @Column(name = "is_active")
    @NotNull(message = "Trạng thái tài khoản không được để trống")
    private Boolean isActive;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_name")
    private Role role;

    @OneToMany(mappedBy = "account")
    private List<BloodRequest> bloodRequests;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private Profile profile;


}
