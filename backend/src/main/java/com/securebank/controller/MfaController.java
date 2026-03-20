package com.securebank.controller;

import com.securebank.dto.ApiResponse;
import com.securebank.dto.MfaResponse;
import com.securebank.security.JwtUtil;
import com.securebank.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/mfa")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
public class MfaController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/enable")
    public ResponseEntity<ApiResponse<MfaResponse>> enableMfa(HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            MfaResponse mfaResponse = userService.enableMfa(userId);
            return ResponseEntity.ok(ApiResponse.success("MFA enabled successfully. Scan the QR code with your authenticator app.", mfaResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<Void>> disableMfa(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            String mfaCode = body.get("mfaCode");
            
            if (mfaCode == null || mfaCode.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("MFA code is required"));
            }

            userService.disableMfa(userId, mfaCode);
            return ResponseEntity.ok(ApiResponse.success("MFA disabled successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyMfa(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            String mfaCode = body.get("mfaCode");
            
            if (mfaCode == null || mfaCode.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("MFA code is required"));
            }

            boolean isValid = userService.verifyMfaCode(userId, mfaCode);
            return ResponseEntity.ok(ApiResponse.success(isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return Long.parseLong(jwtUtil.extractClaim(token, claims -> claims.get("userId").toString()));
    }
}