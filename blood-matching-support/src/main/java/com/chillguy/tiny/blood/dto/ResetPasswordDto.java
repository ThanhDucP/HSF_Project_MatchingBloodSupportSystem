package com.chillguy.tiny.blood.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResetPasswordDto {
    @NotEmpty(message = "Password cannot be empty")
    String password;
    @NotEmpty(message = "Confirm password cannot be empty")
    String confirmPassword;
}