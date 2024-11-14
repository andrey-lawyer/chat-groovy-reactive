package com.chat.dto.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import lombok.Getter

@Getter
class UserRegisterRequestDto {
    @NotBlank(message = "Username is mandatory")
    String username

    @NotBlank(message = "Password is mandatory")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    String password
}
