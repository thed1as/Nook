package com.library.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login request")
public class LoginRequest {
    @Schema(description = "Login email")
    @NotBlank
    private String email;
    @Schema(description = "Login pass")
    @NotBlank
    private String password;
}
