package com.chillguy.tiny.blood.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "blood")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blood {

    @Id
    @Column(name = "blood_code", nullable = false, unique = true)
    private String bloodCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type", nullable = false)
    @NotNull(message = "Nhóm máu không được null")
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rh", nullable = false)
    @NotNull(message = "Rh không được null")
    private RhFactor rh;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    @NotNull(message = "Thành phần máu không được null")
    private ComponentType componentType;

    @Column(name = "is_rare_blood", nullable = false)
    @NotNull(message = "Thông tin máu hiếm không được null")
    private Boolean isRareBlood;

    @Column(name = "quantity", nullable = false)
    @Min(value = 0, message = "Số lượng máu phải ≥ 0")
    private Integer quantity;

    @Column(name = "blood_match")
    private String bloodMatch;

    // --- ENUMs ---
    public enum BloodType {
        A, B, AB, O
    }

    public enum RhFactor {
        POSITIVE, NEGATIVE
    }

    public enum BloodStatus {
        AVAILABLE, USED, EXPIRED
    }

    public enum ComponentType {
        RED_BLOOD_CELL, PLASMA, PLATELET
    }
}
