package com.chillguy.tiny.blood.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

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
}
