package com.chillguy.tiny.blood.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "blood")
public class Blood {

    @Id
    @Column(name = "blood_code", nullable = false, unique = true)
    private String bloodCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type", nullable = false)
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rh", nullable = false)
    private RhFactor rh;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private ComponentType componentType;

    @Column(name = "is_rare_blood", nullable = false)
    private Boolean isRareBlood;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public enum BloodType { A, B, AB, O }
    public enum RhFactor { POSITIVE, NEGATIVE }
    public enum ComponentType { RED_BLOOD_CELL, PLASMA, PLATELET ,WHOLE_BLOOD}

    public String getBloodCodeString() {
        return this.bloodType.name() + (this.rh == RhFactor.POSITIVE ? "+" : "-");
    }
}
