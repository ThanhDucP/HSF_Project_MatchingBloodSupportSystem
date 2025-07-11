package com.chillguy.tiny.blood.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloodRequestDTO {

    @NotBlank(message = "Tên bệnh nhân không được để trống")
    @Size(min = 2, max = 50, message = "Tên bệnh nhân phải từ 2 đến 50 ký tự")
    private String patientName;

    @NotNull(message = "Ngày yêu cầu không được để trống")
    @FutureOrPresent(message = "Ngày yêu cầu phải từ hiện tại trở đi")
    private LocalDate requestDate;

    @NotBlank(message = "Mã nhóm máu không được để trống")
    private String bloodCode;

    private boolean isEmergency;

    @Min(value = 1, message = "Thể tích yêu cầu phải lớn hơn 0 (ml)")
    private int volume;
}
