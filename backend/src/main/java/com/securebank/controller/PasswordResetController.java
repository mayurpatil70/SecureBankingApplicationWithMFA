package com.securebank.controller;

import com.securebank.dto.ApiResponse;
import com.securebank.dto.PasswordResetRequest;
import com.securebank.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password-reset")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Map<String, String>>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        try {
            String token = passwordResetService.createPasswordResetToken(request.getEmail());
            
            // In production, don't return the token!
            // For development/testing, we return it
            return ResponseEntity.ok(ApiResponse.success(
                "Password reset link sent to your email (check console for token)",
                Map.of("token", token, "resetLink", "http://localhost:3000/reset-password?token=" + token)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String token,
            @RequestBody Map<String, String> body) {
        try {
            String newPassword = body.get("newPassword");
            
            if (newPassword == null || newPassword.length() < 8) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password must be at least 8 characters"));
            }

            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        try {
            boolean isValid = passwordResetService.validateToken(token);
            return ResponseEntity.ok(ApiResponse.success(isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}