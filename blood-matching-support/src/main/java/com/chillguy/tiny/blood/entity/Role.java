/*
 * File: Role.java
 * Author: SE184889 - Nguyễn Trần Việt An (AnNTV)
 * Created on: 02-06-2025
 * Purpose: Represents the Role entity used for user authentication and profile management
 *
 * Change Log:
 * [02-06-2025] - Created by: AnNTV
 */

package com.chillguy.tiny.blood.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class
Role {

    @Id
    @Column(name = "role_name", nullable = false)
    @NotBlank(message = "Tên quyền không được để trống")
    @Size(max = 50, message = "Tên quyền tối đa 50 ký tự")
    private String role;

    @Column(name = "description")
    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Account> accounts = new ArrayList<>();
}
