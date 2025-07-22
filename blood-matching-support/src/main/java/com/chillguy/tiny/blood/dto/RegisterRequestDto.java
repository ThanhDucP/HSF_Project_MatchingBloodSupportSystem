package com.chillguy.tiny.blood.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "Xin hãy nhập tên đăng nhập")
    private String username;

    @NotBlank(message = "Xin hãy nhập email")
    private String email;

    @NotBlank(message = "Xin hãy nhập mật khẩu")
    @Size(min = 6, max = 20, message = "Xin hãy nhập mật khẩu từ 6 đến 20 ký tự")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*?])(?=.*[a-zA-Z0-9]).{6,20}$",
            message = "Mật khẩu phải chứa ít nhất một chữ hoa, một ký tự đặc biệt(!@#$%^&*?) và từ 6 đến 20 ký tự")
    private String password;

}