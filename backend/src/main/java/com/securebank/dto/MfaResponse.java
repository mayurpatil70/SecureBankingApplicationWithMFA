package com.securebank.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MfaResponse {
    private String secret;
    private String qrCodeUrl;
    private String qrCodeImage; // Base64 encoded
}