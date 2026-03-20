package com.securebank.controller;

import com.securebank.dto.ApiResponse;
import com.securebank.dto.AuthRequest;
import com.securebank.dto.MfaRequest;
import com.securebank.dto.RegisterRequest;
import com.securebank.dto.AuthResponse;
import com.securebank.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            AuthResponse response = authService.register(request, ipAddress);
            return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            AuthResponse response = authService.login(request, ipAddress);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login/mfa")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithMfa(
            @Valid @RequestBody MfaRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            AuthResponse response = authService.loginWithMfa(request, ipAddress);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}