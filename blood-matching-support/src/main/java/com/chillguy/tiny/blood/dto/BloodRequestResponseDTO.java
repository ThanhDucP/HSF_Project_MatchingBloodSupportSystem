package com.chillguy.tiny.blood.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BloodRequestResponseDTO {
    private String requestId;
    private String patientName;
    private LocalDate requestDate;
    private String bloodCode;
    private boolean isEmergency;
    private int volume;
    private String status;
    private int confirmedCount;
    private String bloodType;
    private String rhFactor;
}
