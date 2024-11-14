package com.chat.dto.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import lombok.Getter

@Getter
class UpdateProfileRequest {

    @NotBlank(message = "Username is mandatory")
    String newUsername;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    String newPassword;

}
