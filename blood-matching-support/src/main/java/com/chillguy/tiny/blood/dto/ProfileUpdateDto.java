package com.chillguy.tiny.blood.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileUpdateDto {

    @NotBlank(message = "Tên không được để trống")
    private String name;

    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải từ 10–11 chữ số")
    private String phone;

    @Past(message = "Ngày sinh phải là trong quá khứ")
    private LocalDate dob;

    @NotNull
    private Boolean gender;

    @NotNull
    private AddressDto address;
}
