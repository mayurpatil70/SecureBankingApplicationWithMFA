package com.securebank.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MfaRequest {
    
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "MFA code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "MFA code must be 6 digits")
    private String mfaCode;
}