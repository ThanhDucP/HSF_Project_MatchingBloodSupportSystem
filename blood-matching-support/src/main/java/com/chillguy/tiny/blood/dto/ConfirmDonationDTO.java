package com.chillguy.tiny.blood.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmDonationDTO {
    private String requestId;
    private String donorEmail;
}
