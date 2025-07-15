package com.chillguy.tiny.blood.dto.response;

import com.chillguy.tiny.blood.dto.AddressDto;
import com.chillguy.tiny.blood.dto.BloodDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileDto {
    private String name;
    private String phone;
    private LocalDate dob;
    private Boolean gender;
    private AddressDto address;
    private long numberOfBloodDonation;
    private BloodDto blood;
    private LocalDate restDate;
}
