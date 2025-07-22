package com.chillguy.tiny.blood.entity;

import jakarta.persistence.*;
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
    private String userName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "is_active")
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
