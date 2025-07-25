package com.chillguy.tiny.blood.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@Builder
public class Address {

    @NotBlank(message = "Thành phố không được để trống")
    private String city;

    @NotBlank(message = "Quận/huyện không được để trống")
    private String district;

    @NotBlank(message = "Phường/xã không được để trống")
    private String ward;

    @NotBlank(message = "Đường không được để trống")
    private String street;

    @NotNull(message = "Latitude không được null")
    @DecimalMin(value = "-90.0", inclusive = true, message = "Latitude không hợp lệ")
    @DecimalMax(value = "90.0", inclusive = true, message = "Latitude không hợp lệ")
    private Double latitude;

    @NotNull(message = "Longitude không được null")
    @DecimalMin(value = "-180.0", inclusive = true, message = "Longitude không hợp lệ")
    @DecimalMax(value = "180.0", inclusive = true, message = "Longitude không hợp lệ")
    private Double longitude;

}
