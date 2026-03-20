package com.securebank.controller;

import com.securebank.dto.ApiResponse;
import com.securebank.dto.ChangePasswordRequest;
import com.securebank.dto.ProfileRequest;
import com.securebank.model.User;
import com.securebank.security.JwtUtil;
import com.securebank.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/profile")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<User>> getProfile(HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            User user = userService.getUserProfile(userId);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @Valid @RequestBody ProfileRequest profileRequest,
            HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            User user = userService.updateProfile(userId, profileRequest);
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest passwordRequest,
            HttpServletRequest request) {
        try {
            Long userId = extractUserId(request);
            userService.changePassword(userId, passwordRequest);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return Long.parseLong(jwtUtil.extractClaim(token, claims -> claims.get("userId").toString()));
    }
}