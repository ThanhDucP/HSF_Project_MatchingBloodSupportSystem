package com.chillguy.tiny.blood.dto.response;

import com.chillguy.tiny.blood.dto.AddressDto;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateResponseDto {
    private String profileId;
    private String name;
    private String phone;
    private LocalDate dob;
    private AddressDto address;
}
