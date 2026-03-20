package com.securebank.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequest {
    
    @NotBlank(message = "Account type is required")
    private String accountType;
    
    @DecimalMin(value = "0.00", message = "Initial deposit must be positive")
    private BigDecimal initialDeposit;
}
